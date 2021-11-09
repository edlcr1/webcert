/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.webcert.web.service.facade.CreateCertificateFromTemplateFacadeService;
import se.inera.intyg.webcert.web.service.utkast.CopyUtkastService;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.util.CopyUtkastServiceHelper;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;

@Service
public class CreateCertificateFromTemplateFacadeServiceImpl implements CreateCertificateFromTemplateFacadeService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateCertificateFromTemplateFacadeServiceImpl.class);

    private final CopyUtkastServiceHelper copyUtkastServiceHelper;
    private final CopyUtkastService copyUtkastService;
    private final UtkastService utkastService;
    private final IntygTextsService intygTextsService;

    @Autowired
    public CreateCertificateFromTemplateFacadeServiceImpl(CopyUtkastServiceHelper copyUtkastServiceHelper,
        CopyUtkastService copyUtkastService, UtkastService utkastService,
        IntygTextsService intygTextsService) {
        this.copyUtkastServiceHelper = copyUtkastServiceHelper;
        this.copyUtkastService = copyUtkastService;
        this.utkastService = utkastService;
        this.intygTextsService = intygTextsService;
    }

    @Override
    public String createCertificateFromTemplate(String certificateId) {
        LOG.debug("Get certificate '{}' that will be used as template", certificateId);
        final var certificate = utkastService.getDraft(certificateId, false);
        final var certificateType = certificate.getIntygsTyp();
        final var newCertificateType = getNewCertificateType(certificateType);
        final var copyRequest = new CopyIntygRequest();
        copyRequest.setPatientPersonnummer(certificate.getPatientPersonnummer());

        LOG.debug("Preparing to create a renewal from template for '{}' with new type '{}' from old type '{}'", certificateId,
            newCertificateType, certificateType);
        final var request = copyUtkastServiceHelper
            .createUtkastFromDifferentIntygTypeRequest(certificateId, newCertificateType, certificateType, copyRequest);

        request.setTypVersion(intygTextsService.getLatestVersion(newCertificateType));

        LOG.debug("Create renewal from template for '{}' with new type '{}' from old type '{}'", certificateId, newCertificateType,
            certificateType);
        final var templateCopy = copyUtkastService.createUtkastFromSignedTemplate(request);

        LOG.debug("Return renewal from template draft '{}' ", templateCopy.getNewDraftIntygId());
        return templateCopy.getNewDraftIntygId();
    }

    private String getNewCertificateType(String templateType) {
        switch (templateType) {
            case "lisjp":
                return "ag7804";
            default:
                return "";
        }
    }
}