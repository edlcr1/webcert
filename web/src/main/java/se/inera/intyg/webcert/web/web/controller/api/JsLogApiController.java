/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.web.controller.api;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.infra.monitoring.annotation.PrometheusTimeMethod;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.ok;
import static javax.ws.rs.core.Response.status;
import static se.inera.intyg.webcert.web.web.controller.api.dto.MonitoringRequest.*;

/**
 * Controller that logs messages from JavaScript to the normal log.
 */
@Path("/jslog")
@Api(value = "jslog", description = "REST API för loggning från frontend till backend-log", produces = MediaType.APPLICATION_JSON)
public class JsLogApiController extends AbstractApiController {

    private static final Logger LOG = LoggerFactory.getLogger(JsLogApiController.class);

    @Autowired
    private MonitoringLogService monitoringService;

    @POST
    @Path("/debug")
    @PrometheusTimeMethod
    public Response debug(String message) {
        LOG.debug(message);
        return ok().build();
    }

    @POST
    @Path("/monitoring")
    @Consumes(APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response monitoring(MonitoringRequest request) {
        if (request == null || !request.isValid()) {
            return status(BAD_REQUEST).build();
        }

        switch (request.getEvent()) {
        case SCREEN_RESOLUTION:
            monitoringService.logScreenResolution(request.getInfo().get(WIDTH), request.getInfo().get(HEIGHT));
            break;
        case DIAGNOSKODVERK_CHANGED:
            monitoringService.logDiagnoskodverkChanged(request.getInfo().get(INTYG_ID), request.getInfo().get(INTYG_TYPE));
            break;
        case SIGNING_FAILED:
            monitoringService.logUtkastSignFailed(request.getInfo().get(ERROR_MESSAGE), request.getInfo().get(INTYG_ID));
            break;
        default:
            return status(BAD_REQUEST).build();
        }
        return ok().build();
    }

    @POST
    @Path("/srs")
    @Consumes(APPLICATION_JSON)
    @PrometheusTimeMethod
    public Response srsMonitoring(MonitoringRequest request) {
        if (request == null || !request.isValid()) {
            return status(BAD_REQUEST).build();
        }
        switch (request.getEvent()) {
            case SRS_LOADED:
                monitoringService.logSrsLoaded(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID),
                        request.getInfo().get(MAIN_DIAGNOSIS_CODE));
                break;
            case SRS_PANEL_ACTIVATED:
                monitoringService.logSrsPanelActivated(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_CONSENT_ANSWERED:
                monitoringService.logSrsConsentAnswered(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_QUESTION_ANSWERED:
                monitoringService.logSrsQuestionAnswered(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_CALCULATE_CLICKED:
                monitoringService.logSrsCalculateClicked(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_HIDE_QUESTIONS_CLICKED:
                monitoringService.logSrsHideQuestionsClicked(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_SHOW_QUESTIONS_CLICKED:
                monitoringService.logSrsShowQuestionsClicked(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_MEASURES_SHOW_MORE_CLICKED:
                monitoringService.logSrsMeasuresShowMoreClicked(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_MEASURES_EXPAND_ONE_CLICKED:
                monitoringService.logSrsMeasuresExpandOneClicked(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_MEASURES_LINK_CLICKED:
                monitoringService.logSrsMeasuresLinkClicked(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_STATISTICS_ACTIVATED:
                monitoringService.logSrsStatisticsActivated(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            case SRS_STATISTICS_LINK_CLICKED:
                monitoringService.logSrsStatisticsLinkClicked(
                        request.getInfo().get(USER_CLIENT_CONTEXT),
                        request.getInfo().get(INTYG_ID),
                        request.getInfo().get(CAREGIVER_ID),
                        request.getInfo().get(CARE_UNIT_ID));
                break;
            default:
                return status(BAD_REQUEST).build();
        }

        return ok().build();
    }

}
