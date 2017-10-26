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
package se.inera.intyg.webcert.web.web.controller.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.feature.WebcertFeature;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.UpdatePatientOnDraftRequest;

/**
 * @author Magnus Ekstrand on 2017-10-09.
 */
@Service
public class IntygIntegrationServiceImpl extends IntegrationServiceImpl {

    @Autowired
    private MonitoringLogService monitoringLog;

    @Autowired
    private UtkastService utkastService;


    // default scope

    @Override
    void ensurePreparation(String intygTyp, String intygId, Utkast utkast, WebCertUser user) {

        if (utkast != null) {
            // INTYG-4086: If the intyg / utkast is authored in webcert, we can check for sekretessmarkering here.
            // If the intyg was authored elsewhere, the check has to be performed after the redirect when the actual intyg
            // is loaded from Intygstjänsten.
            verifySekretessmarkering(utkast, user);

            // INTYG-3212: ArendeDraft patient info should always be up-to-date with the patient info supplied by the
            // integrating journaling system
            if (isUtkast(utkast)) {
                ensureDraftPatientInfoUpdated(intygTyp, intygId, utkast.getVersion(), user);
            }

            // Monitoring log the usage of coherent journaling
            logSammanhallenSjukforing(intygTyp, intygId, utkast, user);
        }

    }


    // default scope

    /**
     * Updates Patient section of a draft with updated patient details for selected types.
     */
    void ensureDraftPatientInfoUpdated(String intygsType, String draftId, long draftVersion, WebCertUser user) {

        // To be allowed to update utkast, we need to have the same authority as when saving a draft..
        authoritiesValidator.given(user, intygsType)
                .features(WebcertFeature.HANTERA_INTYGSUTKAST)
                .privilege(AuthoritiesConstants.PRIVILEGE_SKRIVA_INTYG)
                .orThrow();

        String alternatePatientSSn = user.getParameters().getAlternateSsn();
        Personnummer personnummer = new Personnummer(alternatePatientSSn);
        UpdatePatientOnDraftRequest request = new UpdatePatientOnDraftRequest(personnummer, draftId, draftVersion);
        utkastService.updatePatientOnDraft(request);
    }


    // private stuff

    private void logSammanhallenSjukforing(String intygsTyp, String intygsId, Utkast utkast, WebCertUser user) {
        if (user.getParameters().isSjf()) {
            if (!utkast.getVardgivarId().equals(user.getValdVardgivare().getId())) {
                monitoringLog.logIntegratedOtherCaregiver(intygsId, intygsTyp, utkast.getVardgivarId(), utkast.getEnhetsId());
            } else if (!user.getValdVardenhet().getHsaIds().contains(utkast.getEnhetsId())) {
                monitoringLog.logIntegratedOtherUnit(intygsId, intygsTyp, utkast.getEnhetsId());
            }
        }
    }

}
