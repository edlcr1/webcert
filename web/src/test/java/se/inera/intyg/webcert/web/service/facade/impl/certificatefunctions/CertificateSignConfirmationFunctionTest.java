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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.INTYG_INDICATOR;
import static se.inera.intyg.webcert.web.service.utkast.UtkastServiceImpl.UTKAST_INDICATOR;

import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.infra.security.authorities.AuthoritiesHelper;
import se.inera.intyg.infra.security.common.model.AuthoritiesConstants;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;
import se.inera.intyg.webcert.web.service.utkast.UtkastService;
import se.inera.intyg.webcert.web.service.utkast.dto.PreviousIntyg;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkTypeDTO;

@ExtendWith(MockitoExtension.class)
class CertificateSignConfirmationFunctionTest {

    private static final String DB_TYPE = "DB_TYPE";
    private static final String NOT_DB_TYPE = "NOT_DB_TYPE";
    private static final String PERSON_ID = "19121212-1212";
    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer(PERSON_ID).get();
    private static final String CERTIFICATE_ID = "CERTIFICATE_ID";

    @Mock
    private UtkastService utkastService;

    @Mock
    private AuthoritiesHelper authoritiesHelper;

    @InjectMocks
    private CertificateSignConfirmationFunctionImpl certificateSignConfirmationFunction;

    private WebCertUser webCertUser;
    private Certificate dbCertificate;
    private Certificate notDbCertificate;

    @BeforeEach
    void setUp() {
        webCertUser = mock(WebCertUser.class);

        dbCertificate = new Certificate();
        dbCertificate.setMetadata(CertificateMetadata.builder()
            .id(CERTIFICATE_ID)
            .type(DB_TYPE)
            .patient(Patient.builder()
                .personId(PersonId.builder()
                    .id(PERSON_ID)
                    .build())
                .build())
            .build()
        );

        notDbCertificate = new Certificate();
        notDbCertificate.setMetadata(CertificateMetadata.builder()
            .id(CERTIFICATE_ID)
            .type(NOT_DB_TYPE)
            .patient(Patient.builder()
                .personId(PersonId.builder()
                    .id(PERSON_ID)
                    .build())
                .build())
            .build()
        );
    }

    @Test
    void shallReturnResourceLinkIfSignedDodsbevisExistsForAnotherCareprovider() {
        final var expectedResourceLink = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION,
            "Signera och skicka",
            "Intyget skickas direkt till Skatteverket",
            "Det finns ett signerat dödsbevis för detta personnummer hos annan vårdgivare."
                + " Det är därför inte möjligt att signera detta dödsbevis.",
            true);

        final var previousIntygDifferentCareProvider = Map.of(
            INTYG_INDICATOR,
            Map.of(
                DB_TYPE,
                PreviousIntyg.of(false, false, false, "ENHET", "123", LocalDateTime.now())
            )
        );

        doReturn(previousIntygDifferentCareProvider)
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, webCertUser, CERTIFICATE_ID);
        doReturn(true)
            .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_UNIKT_INTYG, DB_TYPE);

        final var actualResourceLink = certificateSignConfirmationFunction.get(dbCertificate, webCertUser);

        assertEquals(expectedResourceLink, actualResourceLink.get());
    }

    @Test
    void shallReturnNullIfNoSignedDodsbevisExistsForAnotherCareprovider() {
        doReturn(true)
            .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_UNIKT_INTYG, DB_TYPE);

        final var actualResourceLink = certificateSignConfirmationFunction.get(dbCertificate, webCertUser);

        assertTrue(actualResourceLink.isEmpty(), "If no signed dodsbevis exist it should not return a resource link");
    }

    @Test
    void shallReturnResourceLinkIfDraftDodsbevisExistsForAnotherCareprovider() {

        final var expectedResourceLink = ResourceLinkDTO.create(
            ResourceLinkTypeDTO.SIGN_CERTIFICATE_CONFIRMATION,
            "Signera och skicka",
            "Intyget skickas direkt till Skatteverket",
            "Det finns ett utkast på dödsbevis för detta personnummer hos annan vårdgivare."
                + " Senast skapade dödsbevis är det som gäller."
                + " Om du fortsätter och lämnar in dödsbeviset så blir det därför detta dödsbevis som gäller.",
            true);

        final var previousUtkastDifferentCareProvider = Map.of(
            UTKAST_INDICATOR,
            Map.of(
                DB_TYPE,
                PreviousIntyg.of(false, false, false, "ENHET", "123", LocalDateTime.now())
            )
        );

        doReturn(previousUtkastDifferentCareProvider)
            .when(utkastService).checkIfPersonHasExistingIntyg(PERSONNUMMER, webCertUser, CERTIFICATE_ID);
        doReturn(true)
            .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_UNIKT_INTYG, DB_TYPE);

        final var actualResourceLink = certificateSignConfirmationFunction.get(dbCertificate, webCertUser);

        assertEquals(expectedResourceLink, actualResourceLink.get());
    }

    @Test
    void shallReturnNullIfNoDraftNotDodsbevisExistsForAnotherCareprovider() {
        doReturn(false)
            .when(authoritiesHelper).isFeatureActive(AuthoritiesConstants.FEATURE_UNIKT_INTYG, NOT_DB_TYPE);

        final var actualResourceLink = certificateSignConfirmationFunction.get(notDbCertificate, webCertUser);

        assertTrue(actualResourceLink.isEmpty(), "If not dodsbevis it should not return a resource link");
    }
}

