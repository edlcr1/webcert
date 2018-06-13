/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.webcert.web.service.signatur;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.inera.intyg.common.support.common.enumerations.SignaturTyp;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.infra.security.authorities.validation.AuthoritiesValidator;
import se.inera.intyg.infra.security.common.model.AuthenticationMethod;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.infra.security.common.model.IntygUser;
import org.w3._2000._09.xmldsig_.SignatureType;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.PagaendeSignering;
import se.inera.intyg.webcert.persistence.utkast.model.Signatur;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;
import se.inera.intyg.webcert.persistence.utkast.repository.PagaendeSigneringRepository;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.signatur.asn1.ASN1Util;
import se.inera.intyg.webcert.web.service.signatur.dto.SignaturTicket;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.util.UpdateUserUtil;
import se.inera.intyg.webcert.web.web.controller.moduleapi.dto.SignaturData;

import javax.persistence.OptimisticLockException;
import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SignaturServiceImpl implements SignaturService {

    private static final Logger LOG = LoggerFactory.getLogger(SignaturServiceImpl.class);

    private static final String X509_SERIAL = "2.5.4.5";

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private PagaendeSigneringRepository pagaendeSigneringRepository;

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private SignaturTicketTracker ticketTracker;

    @Autowired
    private IntygService intygService;

    @Autowired
    private LogService logService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private MonitoringLogService monitoringService;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired
    private ASN1Util asn1Util;
    private AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();

    @Override
    public SignaturTicket ticketStatus(String ticketId) {
        SignaturTicket ticket = ticketTracker.getTicket(ticketId);
        if (ticket != null && ticket.getId().equals(ticketId)) {
            return ticket;
        } else {
            return new SignaturTicket(ticketId, -1L, SignaturTicket.Status.OKAND, null, 0, null, null,
                    LocalDateTime.now());
        }
    }

    /**
     * Called from the Controller when initiating a client (e.g. NetID) signature. Rewritten in INTYG-5048 so
     * <i>starting</i> a signature process does NOT mutate the Utkast in any way. Instead, a temporary intyg JSON model
     * including the signatureDate and signing identity is stored in a {@link PagaendeSignering} entity.
     * <p>
     * Once the signing has been completed
     * (see {@link SignaturServiceImpl#createAndPersistSignature(Utkast, SignaturTicket, String, WebCertUser)}) the
     * hash, intygsId and version from the JSON model in the PagaendeSignatur is validated and if everything works out,
     * the final state is written to the Utkast table.
     * <p>
     * If the user for some reason failed to finish the signing (cancelled in NetID etc.), the Utkast table won't be
     * affected or contain a signingDate even though it wasn't signed. A stale entry may remain in PAGAENDE_SIGNERING
     * but since those cannot be reused such entries can remain there indefinitely or until cleaned up by a janitor
     * task.
     *
     * @param intygId The id of the draft to generate signing ticket for
     * @param version version
     * @return
     */
    @Override
    @Transactional("jpaTransactionManager")
    public SignaturTicket createDraftHash(String intygId, long version) {
        LOG.debug("Hash for clientsignature of draft '{}'", intygId);

        // Fetch Webcert user
        WebCertUser user = getWebcertUserForSignering();

        // Fetch the certificate draft
        Utkast utkast = getUtkastForSignering(intygId, version, user);

        LocalDateTime signeringstid = LocalDateTime.now();

        try {
            VardpersonReferens vardpersonReferens = UpdateUserUtil.createVardpersonFromWebCertUser(user);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp());
            Vardenhet vardenhetFromJson = moduleApi.getUtlatandeFromJson(utkast.getModel()).getGrundData().getSkapadAv().getVardenhet();
            String updatedInternal = moduleApi
                    .updateBeforeSigning(utkast.getModel(), IntygConverterUtil.buildHosPersonalFromWebCertUser(user, vardenhetFromJson),
                            signeringstid);

            // Skapa ny PagaendeSignering
            PagaendeSignering pagaendeSignering = new PagaendeSignering();
            pagaendeSignering.setIntygData(updatedInternal);
            pagaendeSignering.setIntygsId(utkast.getIntygsId());
            pagaendeSignering.setSigneradAvHsaId(vardpersonReferens.getHsaId());
            pagaendeSignering.setSigneradAvNamn(vardpersonReferens.getNamn());
            pagaendeSignering.setSigneringsDatum(signeringstid);

            pagaendeSignering = pagaendeSigneringRepository.save(pagaendeSignering);

            return createSignaturTicket(utkast.getIntygsId(), pagaendeSignering.getInternReferens(), utkast.getVersion(), updatedInternal,
                    signeringstid);
        } catch (ModuleNotFoundException | IOException | ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Unable to sign certificate: " + e.getMessage());
        }
    }

    private WebCertUser getWebcertUserForSignering() {
        IntygUser user = webCertUserService.getUser();
        AuthoritiesValidator authoritiesValidator = new AuthoritiesValidator();
        if (!authoritiesValidator.given(user).privilege(AuthoritiesConstants.PRIVILEGE_SIGNERA_INTYG).isVerified()) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User is not a doctor. Could not sign utkast.");
        }
        return (WebCertUser) user;
    }

    @Override
    @Transactional("jpaTransactionManager")
    public SignaturTicket clientSignature(String ticketId, String rawSignatur) {

        // Fetch Webcert user
        WebCertUser user = getWebcertUserForSignering();

        // For NetID-based signing, we must match the personId / hsaId on the user principal with the serialNumber
        // extracted from the signature data.
        validateSigningIdentity(user, rawSignatur);

        // Use method common between NetID and BankID to finish signing.
        return finalizeClientSignature(ticketId, rawSignatur, user);
    }

    private void validateSigningIdentity(WebCertUser user, String rawSignatur) {

        try {
            SignaturData asn1SignatureData = new ObjectMapper().readValue(rawSignatur, SignaturData.class);

            // Privatläkare som loggat in med NET_ID-klient måste signera med NetID med samma identitet som i sessionen.
            if (user.isPrivatLakare() && user.getAuthenticationMethod() == AuthenticationMethod.NET_ID) {
                validatePrivatePractitionerSignature(user, asn1SignatureData.getSignatur());
            }

            // Siths-inloggade måste signera med samma SITHS-kort som de loggade in med.
            if (user.getAuthenticationMethod() == AuthenticationMethod.SITHS) {
                validateSithsSignature(user, asn1SignatureData.getSignatur());
            }
        } catch (IOException e) {
            String errMsg = "Cannot finalize signing of Utkast, the ASN.1 signature data from the client could not be parsed: "
                    + e.getMessage();
            LOG.error(errMsg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INDETERMINATE_IDENTITY, errMsg);
        }
    }

    private void validateSithsSignature(WebCertUser user, String rawSignatur) {
        String signaturHsaId = asn1Util.getValue(X509_SERIAL, IOUtils.toInputStream(rawSignatur));

        // If null, there were a problem and no x.520 serialNumber could be found in the signature container.
        if (signaturHsaId == null) {
            String errMsg = "Cannot finalize signing of Utkast, the signature does not contain hsaId in the correct "
                    + "format in the ASN.1 signature data from the client.";
            LOG.error(errMsg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INDETERMINATE_IDENTITY, errMsg);
        }

        if (verifyHsaIdEqual(user, signaturHsaId)) {
            String errMsg = "Cannot finalize signing of Utkast, the logged in user's hsaId and the hsaId in the ASN.1 "
                    + "signature data from the NetID client does not match.";
            LOG.error(errMsg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INDETERMINATE_IDENTITY, errMsg);
        }
    }

    private void validatePrivatePractitionerSignature(WebCertUser user, String rawSignatur) {
        String signaturPersonId = asn1Util.getValue(X509_SERIAL, IOUtils.toInputStream(rawSignatur));

        // If null, there were a problem and no x.520 serialNumber could be found in the signature container.
        if (signaturPersonId == null) {
            String errMsg = "Cannot finalize signing of Utkast, the signature does not contain personId in the correct "
                    + "format in the ASN.1 signature data from the client.";
            LOG.error(errMsg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INDETERMINATE_IDENTITY, errMsg);
        }
        if (verifyPersonIdEqual(user, signaturPersonId)) {
            String errMsg = "Cannot finalize signing of Utkast, the logged in user's personId and the personId in the ASN.1 "
                    + "signature data from the NetID client does not match.";
            LOG.error(errMsg);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INDETERMINATE_IDENTITY, errMsg);
        }
    }

    // Strips off any hyphens
    private boolean verifyPersonIdEqual(WebCertUser user, String signaturPersonId) {
        return !user.getPersonId().trim().replaceAll("\\-", "").equals(signaturPersonId.trim().replaceAll("\\-", ""));
    }

    // We may need to tweak this if it turns out we _somehow_ are getting HsaId's from the sig. that doesn't exactly
    // match what we got from SAML-tickets or HSA on session start. (Kronoberg, see WEBCERT-1501)
    // Consider making a 2-way "subset of" check, if either string is subset of the other, we're OK.
    private boolean verifyHsaIdEqual(WebCertUser user, String signaturHsaId) {
        return signaturHsaId != null && !user.getHsaId().trim().replaceAll("\\-", "")
                .equalsIgnoreCase(signaturHsaId.trim().replaceAll("\\-", ""));
    }

    @Override
    @Transactional("jpaTransactionManager")
    public SignaturTicket clientGrpSignature(String biljettId, String rawSignatur, WebCertUser webCertUser) {
        return finalizeClientSignature(biljettId, rawSignatur, webCertUser);
    }

    private SignaturTicket finalizeClientSignature(String ticketId, String rawSignatur, WebCertUser user) {
        // Lookup signature ticket
        SignaturTicket ticket = ticketTracker.getTicket(ticketId);

        if (ticket == null) {
            LOG.warn("Ticket '{}' hittades ej", ticketId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Biljett " + ticketId + " hittades ej");
        }
        LOG.debug("Klientsignering ticket '{}' intyg '{}'", ticket.getId(), ticket.getIntygsId());

        // Fetch the draft
        Utkast utkast = getUtkastForSignering(ticket.getIntygsId(), ticket.getVersion(), user);

        // Create and persist the new signature
        ticket = createAndPersistSignature(utkast, ticket, rawSignatur, user);

        monitoringService.logIntygSigned(utkast.getIntygsId(), utkast.getIntygsTyp(), user.getHsaId(), user.getAuthenticationScheme(),
                utkast.getRelationKod());

        // Notify stakeholders when certificate has been signed
        notificationService.sendNotificationForDraftSigned(utkast);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        // Note that we explictly supplies the WebCertUser here. The BankID finalization is not executed in a HTTP
        // request context and thus we need to supply the user instance manually.
        logService.logSignIntyg(logRequest, logService.getLogUser(user));

        intygService.handleAfterSigned(utkast);

        return ticketTracker.updateStatus(ticket.getId(), SignaturTicket.Status.SIGNERAD);
    }

    private SignaturTicket createAndPersistSignature(Utkast utkast, SignaturTicket ticket, String rawSignature, WebCertUser user) {

        validateUniqueIntyg(user, utkast);

        PagaendeSignering pagaendeSignering = pagaendeSigneringRepository.findOne(ticket.getPagaendeSigneringId());
        if (pagaendeSignering == null) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Can't complete signing of certificate, no PagaendeSignering found for interreferens "
                            + ticket.getPagaendeSigneringId());
        }
        String payload = pagaendeSignering.getIntygData();

        if (!pagaendeSignering.getIntygsId().equals(utkast.getIntygsId())) {
            LOG.error(
                    "Signing of utkast '{}' failed since the intygsId ({}) on the Utkast is different from the one "
                            + "on the signing operation ({})",
                    utkast.getIntygsId(), pagaendeSignering.getIntygsId());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Internal error signing utkast, the payload of utkast "
                            + utkast.getIntygsId() + " has been modified since signing was initialized");
        }

        if (!ticket.getHash().equals(createHash(payload))) {
            LOG.error("Signing of utkast '{}' failed since the payload has been modified since signing was initialized",
                    utkast.getIntygsId());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Internal error signing utkast, the payload of utkast "
                            + utkast.getIntygsId() + " has been modified since signing was initialized");
        }

        if (utkast.getVersion() != ticket.getVersion()) {
            LOG.error(
                    "Signing of utkast '{}' failed since the version on the utkast ({}) differs from when the signing was initialized ({})",
                    utkast.getIntygsId(), utkast.getVersion(), ticket.getVersion());
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.CONCURRENT_MODIFICATION,
                    "Cannot complete signing, Utkast version differs from signature ticket version.");
        }

        Signatur signatur = new Signatur(ticket.getSigneringstid(), user.getHsaId(), ticket.getIntygsId(), payload, ticket.getHash(),
                rawSignature, SignaturTyp.LEGACY);

        // Update user information ("senast sparat av")
        // Add signature to the utkast and set status as signed
        utkast.setSenastSparadAv(new VardpersonReferens(pagaendeSignering.getSigneradAvHsaId(), pagaendeSignering.getSigneradAvNamn()));
        utkast.setModel(payload);
        utkast.setSignatur(signatur);
        utkast.setStatus(UtkastStatus.SIGNED);

        // Persist utkast with added signature
        Utkast savedUtkast = utkastRepository.save(utkast);

        // Send to Intygstjanst
        intygService.storeIntyg(savedUtkast);

        // Remove PagaendeSignering
        pagaendeSigneringRepository.delete(ticket.getPagaendeSigneringId());

        return ticket;
    }

    @Override
    @Transactional("jpaTransactionManager")
    public SignaturTicket serverSignature(String intygsId, long version) {
        LOG.debug("Signera utkast '{}'", intygsId);

        // On server side we need to create our own signature ticket
        SignaturTicket ticket = createDraftHash(intygsId, version);

        // Fetch Webcert user
        WebCertUser user = getWebcertUserForSignering();

        // Fetch the certificate
        Utkast utkast = getUtkastForSignering(intygsId, ticket.getVersion(), user);

        // Create and persist signature
        ticket = createAndPersistSignature(utkast, ticket, "Signatur", user);

        // Audit signing
        monitoringService.logIntygSigned(utkast.getIntygsId(), utkast.getIntygsTyp(), user.getHsaId(), user.getAuthenticationScheme(),
                utkast.getRelationKod());

        // Notify stakeholders when a draft has been signed
        notificationService.sendNotificationForDraftSigned(utkast);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        logService.logSignIntyg(logRequest);

        intygService.handleAfterSigned(utkast);

        return ticketTracker.updateStatus(ticket.getId(), SignaturTicket.Status.SIGNERAD);
    }

    @Override
    public SignaturTicket clientNiasSignature(String ticketId, SignatureType signatureType, String niasCertificate, WebCertUser user) {
        // Lookup signature ticket
        SignaturTicket ticket = ticketTracker.getTicket(ticketId);

        if (ticket == null) {
            LOG.warn("Ticket '{}' hittades ej", ticketId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE, "Biljett " + ticketId + " hittades ej");
        }
        LOG.debug("Klientsignering ticket '{}' intyg '{}'", ticket.getId(), ticket.getIntygsId());

        // Fetch the draft
        Utkast utkast = getUtkastForSignering(ticket.getIntygsId(), ticket.getVersion(), user);

        // Create and persist the new signature
        StringWriter sw = new StringWriter();
        JAXB.marshal(signatureType, sw);
        String rawSignaturXml = sw.toString();
        ticket = createAndPersistSignature(utkast, ticket, rawSignaturXml, user);

        monitoringService.logIntygSigned(utkast.getIntygsId(), utkast.getIntygsTyp(), user.getHsaId(), user.getAuthenticationScheme(),
                utkast.getRelationKod());

        // Notify stakeholders when certificate has been signed
        notificationService.sendNotificationForDraftSigned(utkast);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);
        // Note that we explictly supplies the WebCertUser here. The NIAS finalization is not executed in a HTTP
        // request context and thus we need to supply the user instance manually.
        logService.logSignIntyg(logRequest, logService.getLogUser(user));

        intygService.handleAfterSigned(utkast);

        return ticketTracker.updateStatus(ticket.getId(), SignaturTicket.Status.SIGNERAD);
    }

    private Utkast getUtkastForSignering(String intygId, long version, WebCertUser user) {
        Utkast utkast = utkastRepository.findOne(intygId);

        if (utkast == null) {
            LOG.warn("Utkast '{}' was not found", intygId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "Internal error signing utkast, the utkast '" + intygId
                            + "' could not be found");
        }

        if (!user.getIdsOfAllVardenheter().contains(utkast.getEnhetsId())) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM,
                    "User does not have privileges to sign utkast '" + intygId + "'");
        } else if (utkast.getVersion() != version) {
            LOG.debug("Utkast '{}' was concurrently modified", intygId);
            throw new OptimisticLockException(utkast.getSenastSparadAv().getNamn());
        } else if (utkast.getStatus() != UtkastStatus.DRAFT_COMPLETE) {
            LOG.warn("Utkast '{}' med status '{}' kunde inte signeras. Måste vara i status {}", intygId, utkast.getStatus(),
                    UtkastStatus.DRAFT_COMPLETE);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE,
                    "Internal error signing utkast, the utkast '" + intygId
                            + "' was not in state " + UtkastStatus.DRAFT_COMPLETE);
        }

        return utkast;
    }

    private SignaturTicket createSignaturTicket(String intygId, long pagaendeSigneringInternreferens, long version, String payload,
            LocalDateTime signeringstid) {
        try {
            String hash = createHash(payload);
            String id = UUID.randomUUID().toString();
            SignaturTicket statusTicket = new SignaturTicket(id, pagaendeSigneringInternreferens, SignaturTicket.Status.BEARBETAR, intygId,
                    version, signeringstid, hash,
                    LocalDateTime.now());
            ticketTracker.trackTicket(statusTicket);
            return statusTicket;
        } catch (IllegalStateException e) {
            LOG.error("Error occured when generating signing hash for intyg {}: {}", intygId, e);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
                    "Internal error signing intyg " + intygId + ", problem when creating signing ticket", e);
        }
    }

    private String createHash(String payload) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            sha.update(payload.getBytes("UTF-8"));
            byte[] digest = sha.digest();
            return new String(Hex.encodeHex(digest));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Validates the utkast to be signed for uniqueness in Webcert.
     * <p>
     * <p>
     * If a blocking intyg is found a {@link WebCertServiceException} is thrown with status code
     * {@link WebCertServiceErrorCodeEnum#INVALID_STATE_INTYG_EXISTS}.
     *
     * @param user   the user, used for accessing the features activated for the utkast.
     * @param utkast the utkast to be signed.
     */
    private void validateUniqueIntyg(WebCertUser user, Utkast utkast) {
        boolean verified = authoritiesValidator.given(user, utkast.getIntygsTyp())
                .features(AuthoritiesConstants.FEATURE_UNIKT_INTYG)
                .isVerified();

        if (verified) {
            List<Utkast> intygList = utkastRepository
                    .findAllByPatientPersonnummerAndIntygsTypIn(
                            utkast.getPatientPersonnummer().getPersonnummerWithDash(),
                            ImmutableSet.of(utkast.getIntygsTyp()))
                    .stream()
                    .filter(oldUtkast -> isBlocking(utkast, oldUtkast))
                    .collect(Collectors.toList());
            if (!intygList.isEmpty()) {
                throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INVALID_STATE_INTYG_EXISTS,
                        "Signed intyg of type " + utkast.getIntygsTyp() + " already exists.");
            }
        }
    }

    /**
     * Determines if the oldUtkast is blocking for the current utkast to be signed.
     *
     * @param utkast    the uktast to be signed.
     * @param oldUtkast the old potentially blocking utkast/intyg
     * @return true if the oldUtkast is infact blocking, false otherwise
     */
    private boolean isBlocking(Utkast utkast, Utkast oldUtkast) {
        // If the utkast is not signed we do not care.
        if (oldUtkast.getSignatur() == null) {
            return false;
        }

        // Revoked intyg is not relevant.
        if (oldUtkast.getAterkalladDatum() != null) {
            return false;
        }

        // Handles ersätta intyg. We do not care which relation is used - this should be handled at the creation of the utkast.
        if (Objects.equals(utkast.getRelationIntygsId(), oldUtkast.getIntygsId())) {
            return false;
        }

        return true;
    }
}
