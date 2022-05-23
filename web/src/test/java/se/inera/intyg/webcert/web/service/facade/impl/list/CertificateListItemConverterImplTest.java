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
package se.inera.intyg.webcert.web.service.facade.impl.list;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.UtkastStatus;
import se.inera.intyg.infra.certificate.dto.CertificateListEntry;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardenhet;
import se.inera.intyg.infra.integration.hsatk.model.legacy.Vardgivare;
import se.inera.intyg.infra.integration.hsatk.services.legacy.HsaOrganizationsService;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.CertificateForwardFunction;
import se.inera.intyg.webcert.web.service.facade.impl.certificatefunctions.ResourceLinkFactory;
import se.inera.intyg.webcert.web.service.facade.list.CertificateListItemConverterImpl;
import se.inera.intyg.webcert.web.service.facade.list.ResourceLinkListHelper;
import se.inera.intyg.webcert.web.service.facade.list.config.dto.ListColumnType;
import se.inera.intyg.webcert.web.service.facade.list.dto.*;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.facade.dto.ResourceLinkDTO;
import se.inera.intyg.webcert.web.web.util.resourcelinks.dto.ActionLink;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CertificateListItemConverterImplTest {

    final String UNIT_NAME = "UNIT_NAME";
    final String CARE_PROVIDER_NAME = "CARE_PROVIDER_NAME";
    final List<ResourceLinkDTO> LINKS = List.of(ResourceLinkFactory.read());

    @Mock
    private HsaOrganizationsService hsaOrganizationsService;
    @Mock
    private ResourceLinkListHelper resourceLinkListHelper;
    @InjectMocks
    private CertificateListItemConverterImpl certificateListItemConverter;

    @Nested
    class ListDrafts {
        @BeforeEach
        public void setup() {
            when(resourceLinkListHelper.get(any(ListIntygEntry.class), any(CertificateListItemStatus.class))).thenReturn(LINKS);
        }

        private final ListType LIST_TYPE = ListType.DRAFTS;

        @Test
        public void shouldSetCertificateId() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

            assertEquals(listIntygEntry.getIntygId(), result.getValue(ListColumnType.CERTIFICATE_ID));
        }

        @Test
        public void shouldSetCertificateTypeName() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

            assertEquals(listIntygEntry.getIntygTypeName(), result.getValue(ListColumnType.CERTIFICATE_TYPE_NAME));
        }

        @Test
        public void shouldSetPatientId() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertEquals(listIntygEntry.getPatientId().getPersonnummerWithDash(), patientListInfo.getId());
        }

        @Test
        public void shouldSetPatientIsProtectedPerson() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertTrue(patientListInfo.isProtectedPerson());
        }

        @Test
        public void shouldSetPatientIsDeceased() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertTrue(patientListInfo.isDeceased());
        }

        @Test
        public void shouldSetPatientIsTestIndicated() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertTrue(patientListInfo.isTestIndicated());
        }

        @Test
        public void shouldSetPatientIsNotProtectedPerson() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), false, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertFalse(patientListInfo.isProtectedPerson());
        }

        @Test
        public void shouldSetPatientIsNotDeceased() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), false, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertFalse(patientListInfo.isDeceased());
        }

        @Test
        public void shouldSetPatientIsNotTestIndicated() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), false, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertFalse(patientListInfo.isTestIndicated());
        }

        @Test
        public void shouldSetDraftStatusComplete() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

            assertEquals(CertificateListItemStatus.COMPLETE.getName(), result.getValue(ListColumnType.STATUS));
        }

        @Test
        public void shouldSetDraftStatusIncomplete() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_INCOMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

            assertEquals(CertificateListItemStatus.INCOMPLETE.getName(), result.getValue(ListColumnType.STATUS));
        }

        @Test
        public void shouldSetDraftStatusLocked() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_LOCKED.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

            assertEquals(CertificateListItemStatus.LOCKED.getName(), result.getValue(ListColumnType.STATUS));
        }

        @Test
        public void shouldSetSaved() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

            assertEquals(listIntygEntry.getLastUpdatedSigned(), result.getValue(ListColumnType.SAVED));
        }

        @Test
        public void shouldSetSavedBy() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);

            assertEquals(listIntygEntry.getUpdatedSignedBy(), result.getValue(ListColumnType.SAVED_BY));
        }

        @Test
        public void shouldSetLinks() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var links = (List<ResourceLinkDTO>) result.getValue(ListColumnType.LINKS);

            assertTrue(links.size() > 0);
            assertEquals(LINKS.get(0), links.get(0));
        }

        @Test
        public void shouldNotSetForwardedInfoIfLinkDoesNotExist() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, false);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var forwarded = result.getValue(ListColumnType.FORWARDED);

            assertNull(forwarded);
        }
    }


    @Nested
    class Forwarded {
        private final ListType LIST_TYPE = ListType.DRAFTS;
        final List<ResourceLinkDTO> LINKS_WITH_FORWARDED = List.of(ResourceLinkFactory.read(), CertificateForwardFunction.createResourceLink());

        @BeforeEach
        public void setup() {
            final var unit = new Vardenhet();
            final var careProvider = new Vardgivare();

            unit.setNamn(UNIT_NAME);
            careProvider.setNamn(CARE_PROVIDER_NAME);

            when(resourceLinkListHelper.get(any(ListIntygEntry.class), any(CertificateListItemStatus.class))).thenReturn(LINKS_WITH_FORWARDED);
            when(hsaOrganizationsService.getVardenhet(anyString())).thenReturn(unit);
            when(hsaOrganizationsService.getVardgivareInfo(anyString())).thenReturn(careProvider);
        }

        @Test
        public void shouldSetIsForwarded() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var forwardedListInfo = (ForwardedListInfo) result.getValue(ListColumnType.FORWARDED);

            assertTrue(forwardedListInfo.isForwarded());
        }

        @Test
        public void shouldSetUnitName() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var forwardedListInfo = (ForwardedListInfo) result.getValue(ListColumnType.FORWARDED);

            assertEquals(UNIT_NAME, forwardedListInfo.getUnitName());
        }

        @Test
        public void shouldSetCareProviderName() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, true);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var forwardedListInfo = (ForwardedListInfo) result.getValue(ListColumnType.FORWARDED);

            assertEquals(CARE_PROVIDER_NAME, forwardedListInfo.getCareProviderName());
        }

        @Test
        public void shouldSetIsNotForwarded() {
            final var listIntygEntry = ListTestHelper.createListIntygEntry(UtkastStatus.DRAFT_COMPLETE.toString(), true, false);
            final var result = certificateListItemConverter.convert(listIntygEntry, LIST_TYPE);
            final var forwardedListInfo = (ForwardedListInfo) result.getValue(ListColumnType.FORWARDED);

            assertFalse(forwardedListInfo.isForwarded());
        }
    }

    @Nested
    class ListSignedCertificates {

        @BeforeEach
        public void setup() {
            when(resourceLinkListHelper.get(any(CertificateListEntry.class), any(CertificateListItemStatus.class))).thenReturn(LINKS);
        }

        @Test
        public void shouldSetLinks() {
            final var entry = ListTestHelper.createCertificateListEntry();
            final var result = certificateListItemConverter.convert(entry);
            final var links = (List<ResourceLinkDTO>) result.getValue(ListColumnType.LINKS);

            assertTrue(links.size() > 0);
            assertEquals(LINKS.get(0), links.get(0));
        }

        @Test
        public void shouldSetCertificateId() {
            final var entry = ListTestHelper.createCertificateListEntry();
            final var result = certificateListItemConverter.convert(entry);

            assertEquals(entry.getCertificateId(), result.getValue(ListColumnType.CERTIFICATE_ID));
        }

        @Test
        public void shouldSetCertificateTypeName() {
            final var entry = ListTestHelper.createCertificateListEntry();
            final var result = certificateListItemConverter.convert(entry);

            assertEquals(entry.getCertificateTypeName(), result.getValue(ListColumnType.CERTIFICATE_TYPE_NAME));
        }

        @Test
        public void shouldSetPatientId() {
            final var entry = ListTestHelper.createCertificateListEntry();
            final var result = certificateListItemConverter.convert(entry);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertEquals(entry.getCivicRegistrationNumber(), patientListInfo.getId());
        }

        @Test
        public void shouldSetPatientIsProtectedPerson() {
            final var entry = ListTestHelper.createCertificateListEntry(false, true, "191212121212");
            final var result = certificateListItemConverter.convert(entry);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertTrue(patientListInfo.isProtectedPerson());
        }

        @Test
        public void shouldSetPatientIsDeceased() {
            final var entry = ListTestHelper.createCertificateListEntry(false, true, "191212121212");
            final var result = certificateListItemConverter.convert(entry);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertTrue(patientListInfo.isDeceased());
        }

        @Test
        public void shouldSetPatientIsTestIndicated() {
            final var entry = ListTestHelper.createCertificateListEntry(false, true, "191212121212");
            final var result = certificateListItemConverter.convert(entry);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertTrue(patientListInfo.isTestIndicated());
        }

        @Test
        public void shouldSetIsSent() {
            final var entry = ListTestHelper.createCertificateListEntry(true, true, "191212121212");
            final var result = certificateListItemConverter.convert(entry);

            assertEquals("Skickat", result.getValue("STATUS"));
        }

        @Test
        public void shouldSetIsNotSent() {
            final var entry = ListTestHelper.createCertificateListEntry(false, true, "191212121212");
            final var result = certificateListItemConverter.convert(entry);

            assertEquals("Ej skickat", result.getValue("STATUS"));
        }

        @Test
        public void shouldSetPatientIsNotProtectedPerson() {
            final var entry = ListTestHelper.createCertificateListEntry(false, false, "191212121212");
            final var result = certificateListItemConverter.convert(entry);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertFalse(patientListInfo.isProtectedPerson());
        }

        @Test
        public void shouldSetPatientIsNotDeceased() {
            final var entry = ListTestHelper.createCertificateListEntry(false, false, "191212121212");
            final var result = certificateListItemConverter.convert(entry);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertFalse(patientListInfo.isDeceased());
        }

        @Test
        public void shouldSetPatientIsNotTestIndicated() {
            final var entry = ListTestHelper.createCertificateListEntry(false, false, "191212121212");
            final var result = certificateListItemConverter.convert(entry);
            final var patientListInfo = (PatientListInfo) result.getValue(ListColumnType.PATIENT_ID);

            assertFalse(patientListInfo.isTestIndicated());
        }

        @Test
        public void shouldSetSigned() {
            final var entry = ListTestHelper.createCertificateListEntry();
            final var result = certificateListItemConverter.convert(entry);

            assertEquals(entry.getSignedDate(), result.getValue(ListColumnType.SIGNED));
        }
    }
}