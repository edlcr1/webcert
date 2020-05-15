/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.srs;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.clinicalprocess.healthcond.srs.getconsent.v1.Samtyckesstatus;
import se.inera.intyg.clinicalprocess.healthcond.srs.getsrsinformation.v3.Utdatafilter;
import se.inera.intyg.clinicalprocess.healthcond.srs.types.v1.EgenBedomningRiskType;
import se.inera.intyg.common.lisjp.support.LisjpEntryPoint;
import se.inera.intyg.common.lisjp.v1.model.internal.LisjpUtlatandeV1;
import se.inera.intyg.common.support.common.enumerations.Diagnoskodverk;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.common.internal.Utlatande;
import se.inera.intyg.common.support.model.converter.util.ConverterException;
import se.inera.intyg.infra.integration.srs.model.SrsCertificate;
import se.inera.intyg.infra.integration.srs.model.SrsForDiagnosisResponse;
import se.inera.intyg.infra.integration.srs.model.SrsQuestion;
import se.inera.intyg.infra.integration.srs.model.SrsQuestionResponse;
import se.inera.intyg.infra.integration.srs.model.SrsResponse;
import se.inera.intyg.infra.integration.srs.services.SrsInfraService;
import se.inera.intyg.schemas.contract.InvalidPersonNummerException;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.repository.UtkastRepository;
import se.inera.intyg.webcert.web.service.diagnos.DiagnosService;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponse;
import se.inera.intyg.webcert.web.service.diagnos.dto.DiagnosResponseType;
import se.inera.intyg.webcert.web.service.intyg.IntygService;
import se.inera.intyg.webcert.web.service.intyg.converter.IntygModuleFacade;
import se.inera.intyg.webcert.web.service.intyg.dto.IntygContentHolder;
import se.inera.intyg.webcert.web.service.log.LogService;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.riv.clinicalprocess.healthcond.certificate.types.v2.ResultCodeEnum;

import java.util.ArrayList;
import java.util.List;

@Service
public class SrsServiceImpl implements SrsService {

    @Autowired
    private SrsInfraService srsInfraService;

    @Autowired
    private LogService logService;

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private DiagnosService diagnosService;

    @Autowired
    private UtkastRepository utkastRepository;

    @Autowired
    private IntygService intygService;

    @Autowired
    private IntygModuleFacade intygModuleFacade;

    private static final Logger LOG = LoggerFactory.getLogger(SrsServiceImpl.class);

    //CHECKSTYLE:OFF ParameterNumber
    @Override
    public SrsResponse getSrs(WebCertUser user, String certificateId, String personalIdentificationNumber, String diagnosisCode,
                              boolean performRiskPrediction, boolean addMeasures, boolean addStatistics,
                              List<SrsQuestionResponse> answers) throws InvalidPersonNummerException {
        LOG.debug("getSrs(user: [not logged], certificateId: {}, personalIdentificationNumber: [not logged], diagnosisCode: {},"
                        + "performRiskPrediction: {}, addMeasures: {}, addStatistics: {}, answers: [not logged])",
                certificateId, diagnosisCode, performRiskPrediction, addMeasures, addStatistics);

        if (user == null) {
            throw new IllegalArgumentException("Missing user object");
        }
        if (Strings.isNullOrEmpty(personalIdentificationNumber)) {
            throw new IllegalArgumentException("Missing personal identification number");
        }
        if (Strings.isNullOrEmpty(diagnosisCode)) {
            throw new IllegalArgumentException("Missing diagnosis code");
        }

        Utdatafilter filter = buildResponseFilter(performRiskPrediction, addMeasures, addStatistics);
        SrsResponse response = srsInfraService
                .getSrs(user, certificateId, createPnr(personalIdentificationNumber), diagnosisCode, filter, answers);
        if (response.getPredictionProbabilityOverLimit() != null) {
            logService.logShowPrediction(personalIdentificationNumber, certificateId);
        }
        decorateWithExtensionChain(response, certificateId);
        decorateWithDiagnosisDescription(response);
        return response;
    }
    //CHECKSTYLE:ON ParameterNumber

    @Override
    public List<SrsQuestion> getQuestions(String diagnosisCode) {
        LOG.debug("getQuestions(diagnosisCode:{}", diagnosisCode);
        if (Strings.isNullOrEmpty(diagnosisCode)) {
            throw new IllegalArgumentException("Missing diagnosis code");
        }
        return srsInfraService.getQuestions(diagnosisCode);
    }

    @Override
    public Samtyckesstatus getConsent(String personalIdentityNumber, String careUnitHsaId) throws InvalidPersonNummerException {
        LOG.debug("getConsent(personalIdentityNumber: [not logged], careUnitHsaId: {})", careUnitHsaId);
        Personnummer p = createPnr(personalIdentityNumber);
        return srsInfraService.getConsent(careUnitHsaId, p);
    }

    @Override
    public ResultCodeEnum setConsent(String personalIdentificationNumber, String careUnitHsaId, boolean consent)
            throws InvalidPersonNummerException {
        LOG.debug("setConsent(personalIdentityNumber: [not logged], careUnitHsaId: {}, consent: {})", careUnitHsaId, consent);
        Personnummer p = createPnr(personalIdentificationNumber);
        return srsInfraService.setConsent(careUnitHsaId, p, consent);
    }

    @Override
    public ResultCodeEnum setOwnOpinion(String personalIdentificationNumber, String careGiverHsaId, String careUnitHsaId,
                                        String certificateId, String diagnosisCode, String opinion) {
        LOG.debug("setOwnOpinion(personalIdentityNumber: [not logged], careGiverHsaId: {}, careUnitHsaId: {}, "
                + "certificateId: {}, diagnosisCode: {})", careGiverHsaId, careUnitHsaId, certificateId, diagnosisCode);
        if (!EnumUtils.isValidEnum(EgenBedomningRiskType.class, opinion)) {
            throw new IllegalArgumentException("Incorrect value for own opinion: " + opinion);
        }
        ResultCodeEnum result = srsInfraService.setOwnOpinion(careGiverHsaId, careUnitHsaId, certificateId, diagnosisCode,
                        EgenBedomningRiskType.fromValue(opinion));
        if (result != ResultCodeEnum.ERROR) {
            // send PDL log event
            logService.logSetOwnOpinion(personalIdentificationNumber, certificateId);
        }
        return result;
    }

    @Override
    public List<String> getAllDiagnosisCodes() {
        LOG.debug("getAllDiagnosisCodes()");
        return srsInfraService.getAllDiagnosisCodes();
    }

    @Override
    public SrsForDiagnosisResponse getSrsForDiagnosis(String diagnosisCode) {
        LOG.debug("getSrsForDiagnosis(diagnosisCode: {})", diagnosisCode);
        final SrsForDiagnosisResponse srsForDiagnose = srsInfraService.getSrsForDiagnose(diagnosisCode);
        monitoringLog.logGetSrsForDiagnose(diagnosisCode);
        return srsForDiagnose;
    }

    /**
     * Uses IntygModuleFacade for LISJP/FK7804 to decode the internal model of the LISJP certificate/draft.
     * @param model model of the certificate/draft
     * @return A LisjpUtlatandeV1 filled with info from the model of the certificate/draft
     * @throws ConverterException if the model is not a lisjp model
     */
    private LisjpUtlatandeV1 getLispjV1UtlatandeFromModel(String model) throws ConverterException {
        Utlatande utlatande = intygModuleFacade.getUtlatandeFromInternalModel(LisjpEntryPoint.MODULE_ID, model);
        if (!(utlatande instanceof LisjpUtlatandeV1)) {
            throw new ConverterException("Utlatande is not of type LisjpUtlatandeV1, utlatande: " + utlatande);
        }
        return (LisjpUtlatandeV1) utlatande;
    }

    /**
     * Uses a LISJP/FK7804 utlatande to gather certificate information for SRS.
     * @param lisjpUtlatandeV1 a LISJP/FK7804 utlatande
     * @return An object holding certificate info to be used by SRS
     * @throws ConverterException if the utlatande is null
     */
    private SrsCertificate buildSrsCertFromUtlatande(LisjpUtlatandeV1 lisjpUtlatandeV1) throws ConverterException {
        if (lisjpUtlatandeV1 == null) {
            throw new ConverterException("Utlatande to convert to SrsCert was null");
        }
        SrsCertificate srsCert = new SrsCertificate(lisjpUtlatandeV1.getId());
        if (lisjpUtlatandeV1.getGrundData() != null && lisjpUtlatandeV1.getGrundData().getSigneringsdatum() != null) {
            srsCert.setSignedDate(lisjpUtlatandeV1.getGrundData().getSigneringsdatum().toLocalDate());
        }
        if (lisjpUtlatandeV1.getDiagnoser() != null && !lisjpUtlatandeV1.getDiagnoser().isEmpty()) {
            srsCert.setMainDiagnosisCode(lisjpUtlatandeV1.getDiagnoser().get(0).getDiagnosKod());
        }
        LOG.debug("SrsCertificate(id:{}, mainDiagCode:{}, signedDate:{})",
                srsCert.getCertificateId(), srsCert.getMainDiagnosisCode(), srsCert.getSignedDate());
        return srsCert;
    }

    /**
     * Looks for a FRLNG relation in an utlatande.
     * @param utlatande the utlatande to look in
     * @return the parent (relation target) certificate id if found, else null
     */
    private String getExtensionCertificateIdFromUtlatande(LisjpUtlatandeV1 utlatande) {
        String parentCertificateId = null;
        if (utlatande != null && utlatande.getGrundData() != null && utlatande.getGrundData().getRelation() != null
                && utlatande.getGrundData().getRelation().getRelationKod() == RelationKod.FRLANG
                && StringUtils.isNotBlank(utlatande.getGrundData().getRelation().getRelationIntygsId())) {
            parentCertificateId = utlatande.getGrundData().getRelation().getRelationIntygsId();
        }
        return parentCertificateId;
    }

    /**
     * Use certificate service to look for a certificate.
     * NB! will not PDL log which needs to be done elsewhere if required.
     * @param certificateId
     * @return
     */
    private IntygContentHolder getCertificate(String certificateId) {
        return intygService.fetchIntygData(certificateId, LisjpEntryPoint.MODULE_ID, false, false);
    }

    /**
     * Checks first in Webcert draft repo and then in the certificate service to find.
     * the given certificate and returns the model.
     * @param certificateId the certificate to look for
     * @return the model of the draft or intyg, or null if no certificate was found
     */
    private String getModelForCertificateId(String certificateId) {
        String currentModel = null;
        // The first one is a draft most of the times and doesn't yet exist in the certificate service
        Utkast currentUtkast = utkastRepository.findById(certificateId).orElse(null);
        if (currentUtkast != null) {
            currentModel = currentUtkast.getModel();
        } else { // If no draft/certificate was found in Webcert, look in the certificate service
            // This will fallback and check in Webcert if it gets no hit in certificate service
            // If we do it the other way around and use this first we will get unnecessary round trips to the
            // certificate service and we will also get an error in the logs each time the certificate is not in
            // the certificate service (i.e. when it is still a draft, which will happen often)
            IntygContentHolder currentCert = getCertificate(certificateId);
            currentModel = currentCert != null ? currentCert.getContents() : null;
        }
        return currentModel;
    }

    /**
     * Decorates the response object with a chain of certificates directly linked parent relations of type extension (FRLANG).
     * E.g. certificateId --extends--> certificateId2 --extends--> certificateId3 --extends---> nothing yields a chain of three
     * certificates.
     * @param response the decorated response object
     * @param certificateId the certificate id of the starting certificate/draft
     */
    protected void decorateWithExtensionChain(SrsResponse response, String certificateId) {
        LOG.debug("decorateWithIntygsKedja(certificateId:{})", certificateId);
        List<SrsCertificate> chain = new ArrayList<>();
        int i = 0;
        try {
            String currentCertificateId = certificateId;
            while (StringUtils.isNotBlank(currentCertificateId)) {
                String currentModel = getModelForCertificateId(currentCertificateId);
                if (currentModel != null) {
                    LisjpUtlatandeV1 currentUtlatande = getLispjV1UtlatandeFromModel(currentModel);
                    chain.add(buildSrsCertFromUtlatande(currentUtlatande));
                    LOG.debug("extensionChain[{}] id:{}, model:{}", i++, currentCertificateId, currentModel);
                    currentCertificateId = getExtensionCertificateIdFromUtlatande(currentUtlatande);
                    LOG.debug("next parentCertificateId: {}", currentCertificateId);
                } else {
                    LOG.debug("No certificate was found with id: {}", currentCertificateId);
                    break;
                }
            }
            response.replaceExtensionChain(chain);
        } catch (ConverterException e) {
            LOG.error("Couldn't convert certificate to correct type while decorating extension chain for SRS", e);
        }
    }

    /**
     * Decorates the response with descriptions of the diagnosis codes that already are in the response.
     */
    private void decorateWithDiagnosisDescription(SrsResponse response) {
        if (!Strings.isNullOrEmpty(response.getPredictionDiagnosisCode())) {
            DiagnosResponse diagnosResponse = diagnosService
                    .getDiagnosisByCode(response.getPredictionDiagnosisCode(), Diagnoskodverk.ICD_10_SE);
            if (diagnosResponse.getResultat() == DiagnosResponseType.OK && diagnosResponse.getDiagnoser() != null
                    && !diagnosResponse.getDiagnoser().isEmpty()) {
                response.setPredictionDiagnosisDescription(diagnosResponse.getDiagnoser().get(0).getBeskrivning());
            }
        }
        if (!Strings.isNullOrEmpty(response.getAtgarderDiagnosisCode())) {
            DiagnosResponse diagnosResponse = diagnosService
                    .getDiagnosisByCode(response.getAtgarderDiagnosisCode(), Diagnoskodverk.ICD_10_SE);
            if (diagnosResponse.getResultat() == DiagnosResponseType.OK && diagnosResponse.getDiagnoser() != null
                    && !diagnosResponse.getDiagnoser().isEmpty()) {
                response.setAtgarderDiagnosisDescription(diagnosResponse.getDiagnoser().get(0).getBeskrivning());
            }
        }
        if (!Strings.isNullOrEmpty(response.getStatistikDiagnosisCode())) {
            DiagnosResponse diagnosResponse = diagnosService
                    .getDiagnosisByCode(response.getStatistikDiagnosisCode(), Diagnoskodverk.ICD_10_SE);
            if (diagnosResponse.getResultat() == DiagnosResponseType.OK && diagnosResponse.getDiagnoser() != null
                    && !diagnosResponse.getDiagnoser().isEmpty()) {
                response.setStatistikDiagnosisDescription(diagnosResponse.getDiagnoser().get(0).getBeskrivning());
            }
        }
    }

    /**
     * Uses incoming parameters to constructs a response data filter to control which parts should be included in the response from
     * the SRS service (UtdataFilter).
     * @param addPrediction true if predictions should be added to the response from SRS
     * @param addMeasures true if measures should be added to the response from SRS
     * @param addStatistics true if statistics should be added to the response from SRS
     * @return A filter object to control the response from the SRS service in accordance with the given parameters
     */
    private Utdatafilter buildResponseFilter(boolean addPrediction, boolean addMeasures, boolean addStatistics) {
        Utdatafilter filter = new Utdatafilter();
        filter.setPrediktion(addPrediction);
        filter.setAtgardsrekommendation(addMeasures);
        filter.setStatistik(addStatistics);
        return filter;
    }

    /**
     * Creates a Personnummer object suitable for communicating with SRS.
     * @param personId A personal identification number as a string on form YYYYMMDDNNNN
     * @return The personal identification number wrapped in a Personnummer object
     * @throws InvalidPersonNummerException If the format is incorrect
     */
    private Personnummer createPnr(String personId) throws InvalidPersonNummerException {
        return Personnummer.createPersonnummer(personId)
                .orElseThrow(() -> new InvalidPersonNummerException("Could not parse personnummer: " + personId));
    }

}
