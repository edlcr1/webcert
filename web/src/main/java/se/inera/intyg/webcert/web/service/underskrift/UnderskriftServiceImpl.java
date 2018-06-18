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
package se.inera.intyg.webcert.web.service.underskrift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.common.support.modules.support.api.exception.ModuleException;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceException;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.converter.util.IntygConverterUtil;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.log.LogRequestFactory;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.log.dto.LogRequest;
import se.inera.intyg.webcert.web.service.notification.NotificationService;
import se.inera.intyg.webcert.web.service.underskrift.fake.FakeUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.grp.GrpUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.nias.NiasUnderskriftService;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.underskrift.xmldsig.XmlUnderskriftServiceImpl;
import se.inera.intyg.webcert.web.service.user.WebCertUserService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import javax.persistence.OptimisticLockException;
import java.io.IOException;
import java.time.LocalDateTime;

import static se.inera.intyg.infra.security.common.model.AuthenticationMethod.BANK_ID;
import static se.inera.intyg.infra.security.common.model.AuthenticationMethod.EFOS;
import static se.inera.intyg.infra.security.common.model.AuthenticationMethod.MOBILT_BANK_ID;

@Service
public class UnderskriftServiceImpl implements UnderskriftService {

    private static final Logger LOG = LoggerFactory.getLogger(UnderskriftServiceImpl.class);

    @Autowired
    private WebCertUserService webCertUserService;

    @Autowired
    private GrpUnderskriftServiceImpl grpUnderskriftService;

    @Autowired
    private XmlUnderskriftServiceImpl xmlUnderskriftService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygModuleRegistry moduleRegistry;

    @Autowired(required = false)
    private FakeUnderskriftService fakeUnderskriftService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LogService logService;

    @Autowired
    private IntygService intygService;

    @Autowired
    private RedisTicketTracker redisTicketTracker;

    @Autowired
    private NiasUnderskriftService niasUnderskriftService;

    @Override
    public SignaturBiljett startSigningProcess(String intygsId, String intygsTyp, long version) {
        WebCertUser user = webCertUserService.getUser();

        // Check if Utkast is eligible for signing right now, if so get it.
        Utkast utkast = getUtkastForSignering(intygsId, version, user);

        // Update JSON with current user as vardperson
        String updatedJson = updateJsonWithVardpersonAndSigneringsTid(utkast, user);

        // Determine which method to use for creating the SignaturBiljett that contains the payload to sign etc.
        SignaturBiljett signaturBiljett = null;
        switch (user.getAuthenticationMethod()) {
        case SITHS:
        case NET_ID:
        case EFOS:
        case FAKE:
            signaturBiljett = xmlUnderskriftService.skapaSigneringsBiljettMedDigest(intygsId, intygsTyp, version, updatedJson);
            break;
        case BANK_ID:
        case MOBILT_BANK_ID:
            signaturBiljett = grpUnderskriftService.skapaSigneringsBiljettMedDigest(intygsId, intygsTyp, version, updatedJson);
            break;
        default:
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.UNKNOWN_INTERNAL_PROBLEM,
                    "Unhandled user state for signing: " + user.getAuthenticationMethod());
        }

        // Finally, for GRP and NIAS, we need to kick off the Collect pollers.
        if (user.getAuthenticationMethod() == EFOS) {
            niasUnderskriftService.startNiasCollectPoller(user.getHsaId(), signaturBiljett);
        }
        if (user.getAuthenticationMethod() == BANK_ID || user.getAuthenticationMethod() == MOBILT_BANK_ID) {
            grpUnderskriftService.startGrpCollectPoller(user.getPersonId(), signaturBiljett);
        }

        return signaturBiljett;
    }

    /**
     * Called through the /api/signature endpoint when finalizing a FakeSignature.
     */
    @Override
    public SignaturBiljett fakeSignature(String intygsId, String intygsTyp, long version, String ticketId) {
        WebCertUser user = webCertUserService.getUser();
        Utkast utkast = getUtkastForSignering(intygsId, version, user);

        SignaturBiljett signaturBiljett = fakeUnderskriftService.finalizeFakeSignature(ticketId, utkast, user);

        finalizeSignature(utkast, user);
        return signaturBiljett;
    }

    /**
     * Called either when:
     * - the /api/signature endpoint when the NetiD plugin has signed the Base64-encoded SignedInfo XML
     * - the NIAS collect returns with a COMPLETE response.
     */
    @Override
    public SignaturBiljett netidSignature(String biljettId, byte[] signatur, String certifikat) {
        SignaturBiljett sb = redisTicketTracker.findBiljett(biljettId);

        WebCertUser user = webCertUserService.getUser();
        Utkast utkast = getUtkastForSignering(sb.getIntygsId(), sb.getVersion(), user);

        sb = xmlUnderskriftService.finalizeSignature(sb, signatur, certifikat, utkast, user);
        finalizeSignature(utkast, user);
        return sb;
    }

    @Override
    public SignaturBiljett grpSignature(String biljettId, byte[] signatur) {
        SignaturBiljett sb = redisTicketTracker.findBiljett(biljettId);

        WebCertUser user = webCertUserService.getUser();
        Utkast utkast = getUtkastForSignering(sb.getIntygsId(), sb.getVersion(), user);

        sb = grpUnderskriftService.finalizeSignature(sb, signatur, null, utkast, user);
        finalizeSignature(utkast, user);
        return sb;
    }

    @Override
    public SignaturBiljett signeringsStatus(String ticketId) {
        SignaturBiljett sb = redisTicketTracker.findBiljett(ticketId);
        if (sb == null) {
            LOG.error("No SignaturBiljett found for ticketId '{}'", ticketId);
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.DATA_NOT_FOUND,
                    "No SignaturBiljett found for ticketId '" + ticketId + "'");
        }
        return sb;
    }

    private void finalizeSignature(Utkast utkast, WebCertUser user) {
        // Notify stakeholders when certificate has been signed
        notificationService.sendNotificationForDraftSigned(utkast);

        LogRequest logRequest = LogRequestFactory.createLogRequestFromUtkast(utkast);

        // Note that we explictly supplies the WebCertUser here. The NIAS finalization is not executed in a HTTP
        // request context and thus we need to supply the user instance manually.
        logService.logSignIntyg(logRequest, logService.getLogUser(user));

        // Sends intyg to Intygstjänsten.
        intygService.handleAfterSigned(utkast);
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

    private String updateJsonWithVardpersonAndSigneringsTid(Utkast utkast, WebCertUser user) {
        LocalDateTime signeringstid = LocalDateTime.now();

        try {
            // VardpersonReferens vardpersonReferens = UpdateUserUtil.createVardpersonFromWebCertUser(user);
            ModuleApi moduleApi = moduleRegistry.getModuleApi(utkast.getIntygsTyp());
            Vardenhet vardenhetFromJson = moduleApi.getUtlatandeFromJson(utkast.getModel()).getGrundData().getSkapadAv().getVardenhet();
            return moduleApi
                    .updateBeforeSigning(utkast.getModel(), IntygConverterUtil.buildHosPersonalFromWebCertUser(user, vardenhetFromJson),
                            signeringstid);

        } catch (ModuleNotFoundException | IOException | ModuleException e) {
            throw new WebCertServiceException(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM,
                    "Unable to sign certificate: " + e.getMessage());
        }
    }
}