/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.internalapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.webcert.web.service.unansweredcommunication.UnansweredCommunicationService;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationRequest;
import se.inera.intyg.webcert.web.web.controller.internalapi.dto.UnansweredCommunicationResponse;

@ExtendWith(MockitoExtension.class)
class UnansweredCommunicationControllerTest {

    @Mock
    private UnansweredCommunicationService unansweredCommunicationService;
    @InjectMocks
    private UnansweredCommunicationController unansweredCommunicationController;

    @Test
    void shouldCallUnansweredCommunicationService() {
        final var request = new UnansweredCommunicationRequest();
        unansweredCommunicationController.getUnansweredCommunications(request);
        verify(unansweredCommunicationService).get(request);
    }

    @Test
    void shouldReturnUnansweredCommunicationResponse() {
        final var response = new UnansweredCommunicationResponse();
        final var request = new UnansweredCommunicationRequest();
        doReturn(response).when(unansweredCommunicationService).get(request);

        final var result = unansweredCommunicationController.getUnansweredCommunications(request);
        assertEquals(response, result);
    }
}
