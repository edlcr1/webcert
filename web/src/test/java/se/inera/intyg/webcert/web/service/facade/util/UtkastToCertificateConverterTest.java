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

package se.inera.intyg.webcert.web.service.facade.util;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.services.texts.IntygTextsService;
import se.inera.intyg.common.support.facade.builder.CertificateBuilder;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Patient;
import se.inera.intyg.common.support.facade.model.PersonId;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.api.ModuleApi;
import se.inera.intyg.schemas.contract.Personnummer;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.persistence.utkast.model.VardpersonReferens;

@ExtendWith(MockitoExtension.class)
public class UtkastToCertificateConverterTest {

    @Mock
    private IntygModuleRegistry moduleRegistry;

    @Mock
    private IntygTextsService intygTextsService;

    @Mock
    private PatientConverter patientConverter;

    @Mock
    private CertificateRelationsConverter certificateRelationsConverter;

    @InjectMocks
    private UtkastToCertificateConverterImpl utkastToCertificateConverter;

    private final Utkast draft = createDraft();
    private final CertificateRelations certificateRelations = CertificateRelations.builder().build();
    private final Patient patient = getPatient();

    @BeforeEach
    void setupMocks() throws Exception {
        final var moduleApi = mock(ModuleApi.class);
        doReturn(moduleApi)
            .when(moduleRegistry).getModuleApi(draft.getIntygsTyp(), draft.getIntygTypeVersion());

        doReturn(createCertificate())
            .when(moduleApi).getCertificateFromJson(draft.getModel());

        doReturn(certificateRelations)
            .when(certificateRelationsConverter).convert(draft.getIntygsId());

        doReturn(patient)
            .when(patientConverter).convert(
                draft.getPatientPersonnummer(),
                draft.getIntygsTyp(),
                draft.getIntygTypeVersion()
            );
    }

    @Nested
    class ValidateCommonMetadata {

        @Test
        void shallIncludeCreatedDateTime() {
            final var expectedCreated = draft.getSkapad();

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedCreated, actualCertificate.getMetadata().getCreated());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 1, 100})
        void shallIncludeVersion(int expectedVersion) {
            draft.setVersion(expectedVersion);

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedVersion, actualCertificate.getMetadata().getVersion());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shallIncludeForwarded(boolean expectedForwarded) {
            draft.setVidarebefordrad(expectedForwarded);

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedForwarded, actualCertificate.getMetadata().isForwarded());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shallIncludeTestCertificate(boolean expectedTestCertificate) {
            draft.setTestIntyg(expectedTestCertificate);

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedTestCertificate, actualCertificate.getMetadata().isTestCertificate());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void shallIncludeLatestMajorVersion(boolean expectedLatestMajorVersion) {
            doReturn(expectedLatestMajorVersion).when(intygTextsService).isLatestMajorVersion(any(), any());
            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedLatestMajorVersion, actualCertificate.getMetadata().isLatestMajorVersion());
        }
    }

    @Nested
    class ValidateUnit {

        @Test
        void shallContainCompleteUnitData() {
            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertAll(
                () -> assertNotNull(actualCertificate.getMetadata().getUnit().getUnitId(), "UnitId should not be null"),
                () -> assertNotNull(actualCertificate.getMetadata().getUnit().getUnitName(), "UnitName should not be null"),
                () -> assertNotNull(actualCertificate.getMetadata().getUnit().getAddress(), "Address should not be null"),
                () -> assertNotNull(actualCertificate.getMetadata().getUnit().getZipCode(), "ZipCode should not be null"),
                () -> assertNotNull(actualCertificate.getMetadata().getUnit().getCity(), "City should not be null"),
                () -> assertNotNull(actualCertificate.getMetadata().getUnit().getEmail(), "Email should not be null"),
                () -> assertNotNull(actualCertificate.getMetadata().getUnit().getPhoneNumber(), "Phonenumber should not be null")
            );
        }
    }

    @Nested
    class ValidateCareProvider {

        @Test
        void shallIncludeCareProviderId() {
            final var expectedCareProviderId = "CareProviderId";
            draft.setVardgivarId(expectedCareProviderId);

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedCareProviderId, actualCertificate.getMetadata().getCareProvider().getUnitId());
        }

        @Test
        void shallIncludeCareProviderName() {
            final var expectedCareProviderName = "CareProviderName";
            draft.setVardgivarNamn(expectedCareProviderName);

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedCareProviderName, actualCertificate.getMetadata().getCareProvider().getUnitName());
        }
    }

    @Nested
    class ValidateIssuedBy {

        @Test
        void shallIncludePersonId() {
            final var expectedPersonId = "PersonId";
            draft.getSkapadAv().setHsaId(expectedPersonId);

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedPersonId, actualCertificate.getMetadata().getIssuedBy().getPersonId());
        }

        @Test
        void shallIncludeName() {
            final var expectedFullName = "Doctor Alpha";
            draft.getSkapadAv().setNamn(expectedFullName);

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedFullName, actualCertificate.getMetadata().getIssuedBy().getFullName());
        }
    }

    @Test
    void shallIncludePatient() {
        final var expectedPatient = getPatient();
        final var actualCertificate = utkastToCertificateConverter.convert(draft);
        assertEquals(expectedPatient, actualCertificate.getMetadata().getPatient());
    }

    @Test
    void shallIncludeCertificateRelations() {
        final var actualCertificate = utkastToCertificateConverter.convert(draft);
        assertEquals(certificateRelations, actualCertificate.getMetadata().getRelations());
    }

    @Nested
    class ValidateStatus {

        @Test
        void shallIncludeStatusUnsigned() {
            final var expectedStatus = CertificateStatus.UNSIGNED;

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }

        @Test
        void shallIncludeStatusSigned() {
            final var expectedStatus = CertificateStatus.SIGNED;
            draft.setStatus(UtkastStatus.SIGNED);

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }

        @Test
        void shallIncludeStatusRevoked() {
            final var expectedStatus = CertificateStatus.REVOKED;
            draft.setStatus(UtkastStatus.SIGNED);
            draft.setAterkalladDatum(LocalDateTime.now());

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }

        @Test
        void shallIncludeStatusLocked() {
            final var expectedStatus = CertificateStatus.LOCKED;
            draft.setStatus(UtkastStatus.DRAFT_LOCKED);

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }

        @Test
        void shallIncludeStatusLockedRevoked() {
            final var expectedStatus = CertificateStatus.LOCKED_REVOKED;
            draft.setStatus(UtkastStatus.DRAFT_LOCKED);
            draft.setAterkalladDatum(LocalDateTime.now());

            final var actualCertificate = utkastToCertificateConverter.convert(draft);

            assertEquals(expectedStatus, actualCertificate.getMetadata().getStatus());
        }
    }

    private Utkast createDraft() {
        final var draft = new Utkast();
        draft.setIntygsId("certificateId");
        draft.setIntygsTyp("certificateType");
        draft.setIntygTypeVersion("certificateTypeVersion");
        draft.setModel("draftJson");
        draft.setStatus(UtkastStatus.DRAFT_INCOMPLETE);
        draft.setSkapad(LocalDateTime.now());
        draft.setPatientPersonnummer(Personnummer.createPersonnummer("191212121212").orElseThrow());
        draft.setSkapadAv(new VardpersonReferens("personId", "personName"));
        return draft;
    }

    private Certificate createCertificate() {
        return CertificateBuilder.create()
            .metadata(
                CertificateMetadata.builder()
                    .id("certificateId")
                    .type("certificateType")
                    .typeVersion("certificateTypeVersion")
                    .unit(
                        Unit.builder()
                            .unitId("unitId")
                            .unitName("unitName")
                            .address("address")
                            .zipCode("zipCode")
                            .city("city")
                            .email("email")
                            .phoneNumber("phoneNumber")
                            .build()
                    )
                    .build()
            )
            .build();
    }

    private Patient getPatient() {
        return Patient.builder()
            .personId(getPersonId())
            .firstName("Fornamnet")
            .lastName("Efternamnet")
            .middleName("Mellannamnet")
            .build();
    }

    private PersonId getPersonId() {
        final var expectedPersonId = PersonId.builder()
            .id(draft.getPatientPersonnummer().getPersonnummer())
            .type("PERSON_NUMMER")
            .build();
        return expectedPersonId;
    }
}