/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions;

import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.ERSATT_INDICATOR;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.INTYG_INDICATOR;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.UTKAST_INDICATOR;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@Component
public class CertificateSignConfirmationFunctionImpl implements CertificateSignConfirmationFunction {

    private final UtkastService utkastService;
    private final AuthoritiesHelper authoritiesHelper;

    @Autowired
    public CertificateSignConfirmationFunctionImpl(UtkastService utkastService,
        AuthoritiesHelper authoritiesHelper) {
        this.utkastService = utkastService;
        this.authoritiesHelper = authoritiesHelper;
    }

    @Override
    public Optional<ResourceLinkDTO> get(Certificate certificate, WebCertUser webCertUser) {
        if (!isUniqueFeatureActive(certificate)) {
            return Optional.empty();
        }
        final var certificateType = certificate.getMetadata().getType();
        final var personnummer = getPersonnummer(certificate);
        final var existingIntyg = getExistingIntyg(certificate, webCertUser, personnummer);
        final var previousIntygMap = existingIntygWithStatus(existingIntyg, INTYG_INDICATOR);
        final var previousUtkastMap = existingIntygWithStatus(existingIntyg, UTKAST_INDICATOR);
        final var previousReplacedMap = existingIntygWithStatus(existingIntyg, ERSATT_INDICATOR);
        if (previousIntygMap.containsKey(certificateType) && !previousReplacedMap.containsKey(certificateType)) {
            return Optional.of(ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION,
                "Signera och skicka",
                "Intyget skickas direkt till Skatteverket",
                "Det finns ett signerat dödsbevis för detta personnummer hos annan vårdgivare."
                    + " Det är därför inte möjligt att signera detta dödsbevis.",
                true));
        }
        if (previousUtkastMap.containsKey(certificateType) || previousReplacedMap.containsKey(certificateType)) {
            return Optional.of(ResourceLinkDTO.create(
                ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION,
                "Signera och skicka",
                "Intyget skickas direkt till Skatteverket",
                "Det finns ett utkast på dödsbevis för detta personnummer hos annan vårdgivare."
                    + " Senast skapade dödsbevis är det som gäller. Om du fortsätter och lämnar in "
                    + "dödsbeviset så blir det därför detta dödsbevis som gäller.",
                true));
        }
        return Optional.empty();
    }

    private boolean isUniqueFeatureActive(Certificate certificate) {
        return authoritiesHelper.isFeatureActive(AuthoritiesConstants.FEATURE_UNIKT_INTYG, certificate.getMetadata().getType());
    }

    private static Personnummer getPersonnummer(Certificate certificate) {
        return Personnummer.createPersonnummer(certificate.getMetadata().getPatient().getPersonId().getId()).get();
    }

    private Map<String, Map<String, PreviousIntyg>> getExistingIntyg(Certificate certificate, WebCertUser webCertUser,
        Personnummer personnummer) {
        return utkastService.checkIfPersonHasExistingIntyg(personnummer, webCertUser, certificate.getMetadata().getId());
    }

    private static Map<String, PreviousIntyg> existingIntygWithStatus(Map<String, Map<String, PreviousIntyg>> existingIntyg,
        String intygType) {
        return existingIntyg.getOrDefault(intygType, Collections.emptyMap());
    }
}
