/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.integration.v3;

import org.apache.cxf.annotations.SchemaValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.common.support.integration.converter.util.ResultTypeUtil;
import se.inera.intyg.common.support.model.common.internal.Vardenhet;
import se.inera.intyg.common.support.model.common.internal.Vardgivare;
import se.inera.intyg.infra.integration.hsa.services.HsaPersonService;
import se.inera.intyg.infra.security.common.model.IntygUser;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.integration.registry.IntegreradeEnheterRegistry;
import se.inera.intyg.webcert.web.integration.registry.dto.IntegreradEnhetEntry;
import se.inera.intyg.webcert.web.integration.util.HoSPersonEnhetHelper;
import se.inera.intyg.webcert.web.integration.v3.builder.CreateNewDraftRequestBuilder;
import se.inera.intyg.webcert.web.integration.v3.validator.CreateDraftCertificateValidator;
import se.inera.intyg.webcert.web.integration.validator.ResultValidator;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.CreateNewDraftRequest;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.CreateDraftCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.createdraftcertificateresponder.v3.Intyg;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@SchemaValidation
public class CreateDraftCertificateResponderImpl implements CreateDraftCertificateResponderInterface {

    private static final Logger LOG = LoggerFactory.getLogger(CreateDraftCertificateResponderImpl.class);

    @Autowired
    private UtkastService utkastService;

    @Autowired
    private HsaPersonService hsaPersonService;

    @Autowired
    private CreateNewDraftRequestBuilder draftRequestBuilder;

    @Autowired
    private CreateDraftCertificateValidator validator;

    @Autowired
    private IntegreradeEnheterRegistry integreradeEnheterRegistry;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Autowired
    private WebcertUserDetailsService webcertUserDetailsService;

    @Override
    public CreateDraftCertificateResponseType createDraftCertificate(String logicalAddress, CreateDraftCertificateType parameters) {

        Intyg utkastsParams = parameters.getIntyg();

        String invokingUserHsaId = utkastsParams.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utkastsParams.getSkapadAv().getEnhet().getEnhetsId().getExtension();

        IntygUser user;
        try {
            user = webcertUserDetailsService.loadUserByHsaId(invokingUserHsaId);
        } catch (Exception e) {
            return createMIUErrorResponse(utkastsParams);
        }

        // Validate draft parameters
        ResultValidator resultsValidator = validator.validate(utkastsParams);
        if (resultsValidator.hasErrors()) {
            return createValidationErrorResponse(resultsValidator);
        }

        ResultValidator appErrorsValidator = validator.validateApplicationErrors(utkastsParams, user);
        if (appErrorsValidator.hasErrors()) {
            return createApplicationErrorResponse(appErrorsValidator);
        }

        LOG.debug("Creating draft for invoker '{}' on unit '{}'", utkastsParams.getSkapadAv().getPersonalId().getExtension(),
                invokingUnitHsaId);

        // Check if the invoking health personal has MIU rights on care unit
        if (!HoSPersonEnhetHelper.findVardenhetEllerMottagning(user, invokingUnitHsaId).isPresent()) {
            return createMIUErrorResponse(utkastsParams);
        }

        // Create the draft
        Utkast utkast = createNewDraft(utkastsParams, user);

        return createSuccessResponse(utkast.getIntygsId(), invokingUnitHsaId);
    }

    private Utkast createNewDraft(Intyg utkastRequest, IntygUser user) {

        LOG.debug("Creating draft for invoker '{}' on unit '{}'", utkastRequest.getSkapadAv().getPersonalId().getExtension(),
                utkastRequest.getSkapadAv().getEnhet().getEnhetsId().getExtension());

        // Create draft request
        CreateNewDraftRequest draftRequest = draftRequestBuilder.buildCreateNewDraftRequest(utkastRequest, user);

        // Add the creating vardenhet to registry
        addVardenhetToRegistry(draftRequest);

        // Create draft and return its id
        return utkastService.createNewDraft(draftRequest);
    }

    /**
     * The response sent back to caller when an error is raised.
     */
    private CreateDraftCertificateResponseType createErrorResponse(String errorMsg, ErrorIdType errorType) {
        ResultType result = ResultTypeUtil.errorResult(errorType, errorMsg);

        CreateDraftCertificateResponseType response = new CreateDraftCertificateResponseType();
        response.setResult(result);
        return response;
    }

    /**
     * Builds a specific MIU error response.
     */
    private CreateDraftCertificateResponseType createMIUErrorResponse(Intyg utkastType) {

        String invokingUserHsaId = utkastType.getSkapadAv().getPersonalId().getExtension();
        String invokingUnitHsaId = utkastType.getSkapadAv().getEnhet().getEnhetsId().getExtension();

        monitoringLogService.logMissingMedarbetarUppdrag(invokingUserHsaId, invokingUnitHsaId);

        String errMsg = String.format("No valid MIU was found for person %s on unit %s, can not create draft!", invokingUserHsaId,
                invokingUnitHsaId);
        return createErrorResponse(errMsg, ErrorIdType.VALIDATION_ERROR);
    }

    /**
     * Builds a specific validation error response.
     */
    private CreateDraftCertificateResponseType createValidationErrorResponse(ResultValidator resultsValidator) {
        String errMsgs = resultsValidator.getErrorMessagesAsString();
        LOG.warn("Intyg did not validate correctly: {}", errMsgs);
        return createErrorResponse(errMsgs, ErrorIdType.VALIDATION_ERROR);
    }

    /**
     * Builds a specific application error response.
     */
    private CreateDraftCertificateResponseType createApplicationErrorResponse(ResultValidator resultsValidator) {
        String errMsgs = resultsValidator.getErrorMessagesAsString();
        LOG.warn("Intyg did not pass APPLICATION_ERROR check correctly: {}", errMsgs);
        return createErrorResponse(errMsgs, ErrorIdType.APPLICATION_ERROR);
    }

    /**
     * The response sent back to caller when creating a certificate draft succeeded.
     */
    private CreateDraftCertificateResponseType createSuccessResponse(String nyttUtkastsId, String invokingUnitHsaId) {
        ResultType result = ResultTypeUtil.okResult();

        IntygId intygId = new IntygId();
        intygId.setRoot(invokingUnitHsaId);
        intygId.setExtension(nyttUtkastsId);

        CreateDraftCertificateResponseType response = new CreateDraftCertificateResponseType();
        response.setResult(result);
        response.setIntygsId(intygId);
        return response;
    }

    private void addVardenhetToRegistry(CreateNewDraftRequest utkastsRequest) {

        Vardenhet vardenhet = utkastsRequest.getHosPerson().getVardenhet();
        Vardgivare vardgivare = vardenhet.getVardgivare();

        IntegreradEnhetEntry integreradEnhet = new IntegreradEnhetEntry(vardenhet.getEnhetsid(),
                vardenhet.getEnhetsnamn(), vardgivare.getVardgivarid(), vardgivare.getVardgivarnamn());

        integreradeEnheterRegistry.putIntegreradEnhet(integreradEnhet, false, true);
    }

}
