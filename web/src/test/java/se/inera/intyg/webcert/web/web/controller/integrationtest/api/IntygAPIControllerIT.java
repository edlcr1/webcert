/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

package se.inera.intyg.webcert.web.web.controller.integrationtest.api;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.webcert.common.service.exception.WebCertServiceErrorCodeEnum;
import se.inera.intyg.webcert.web.security.WebCertUserOriginType;
import se.inera.intyg.webcert.web.web.controller.api.dto.CopyIntygRequest;
import se.inera.intyg.webcert.web.web.controller.api.dto.ListIntygEntry;
import se.inera.intyg.webcert.web.web.controller.api.dto.NotifiedState;
import se.inera.intyg.webcert.web.web.controller.integrationtest.BaseRestIntegrationTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;

/**
 * Basic test suite that verifies that the endpoint (/api/intyg) for generic intygs operations (list
 * drafts/copy/notification status)
 * are available and reponds according to specification.
 * <p/>
 * Created by marced on 01/12/15.
 */
public class IntygAPIControllerIT extends BaseRestIntegrationTest {

    @Test
    public void testGetListDraftsAndIntygForPersonWithEmptyBackendStore() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        given().pathParam("personNummer", DEFAULT_PATIENT_PERSONNUMMER).expect().statusCode(200).when().get("api/intyg/person/{personNummer}").
                then().
                body(equalTo("[]"));
    }

    @Test
    public void testGetListDraftsAndIntygForPersonWithNonEmptyBackendStore() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        ListIntygEntry[] intygArray =
                given().pathParam("personNummer", DEFAULT_PATIENT_PERSONNUMMER).expect().statusCode(200).when()
                        .get("api/intyg/person/{personNummer}").
                        then().
                        body(matchesJsonSchemaInClasspath("jsonschema/webcert-get-utkast-list-response-schema.json")).extract().response()
                        .as(ListIntygEntry[].class);

        assertEquals(1, intygArray.length);

        assertEquals(utkastId, intygArray[0].getIntygId());
        assertEquals(DEFAULT_PATIENT_PERSONNUMMER, intygArray[0].getPatientId().getPersonnummer());
        assertEquals("fk7263", intygArray[0].getIntygType());
    }

    @Test
    public void testCreateNewUtkastCopyBasedOnExistingUtkast() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).expect().statusCode(200).
                when().post("api/intyg/{intygsTyp}/{intygsId}/kopiera").
                then().
                body("intygsUtkastId", not(isEmptyString())).
                body("intygsUtkastId", not(equalTo(utkastId))).
                body("intygsTyp", equalTo("fk7263"));
    }

    @Test
    public void testCreateNewUtkastCopyBasedOnExistingUtkastFailsForMissingPriveliges() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        // Lakare role has the KOPIERA_INTYG priviledge, but the privilege is restricted to origintype=NORMAL /
        // DJUPINTEGRERAD.
        // We change the current users origin to be uthop which should trigger an auth exception response.
        changeOriginTo(WebCertUserOriginType.UTHOPP.name());

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).
                expect().statusCode(500).
                when().post("api/intyg/{intygsTyp}/{intygsId}/kopiera").
                then().
                body("errorCode", equalTo(WebCertServiceErrorCodeEnum.AUTHORIZATION_PROBLEM.name())).
                body("message", not(isEmptyString()));

    }

    /**
     * Check that trying to copy utkast with bad input gives error response.
     */
    @Test
    public void testCreateNewUtkastCopyBasedOnExistingUtkastWithInvalidPatientpersonNummer() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        CopyIntygRequest copyIntygRequest = new CopyIntygRequest();
        copyIntygRequest.setPatientPersonnummer(null);
        copyIntygRequest.setNyttPatientPersonnummer(new Personnummer(DEFAULT_PATIENT_PERSONNUMMER));

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);

        given().contentType(ContentType.JSON).and().pathParams(pathParams).and().body(copyIntygRequest).expect().statusCode(500).
                when().post("api/intyg/{intygsTyp}/{intygsId}/kopiera").then().
                body("errorCode", equalTo(WebCertServiceErrorCodeEnum.INTERNAL_PROBLEM.name())).
                body("message", not(isEmptyString()));

    }

    @Test
    public void testNotifiedOnUtkast() {

        RestAssured.sessionId = getAuthSession(DEFAULT_LAKARE);

        String utkastId = createUtkast("fk7263", DEFAULT_PATIENT_PERSONNUMMER);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("intygsTyp", "fk7263");
        pathParams.put("intygsId", utkastId);
        pathParams.put("version", "0");

        NotifiedState notifiedState = new NotifiedState();
        notifiedState.setNotified(true);

        ListIntygEntry updatedIntyg =
                given().contentType(ContentType.JSON).and().body(notifiedState).and().pathParams(pathParams).expect().statusCode(200).when()
                        .put("api/intyg/{intygsTyp}/{intygsId}/{version}/vidarebefordra").
                        then().
                        body(matchesJsonSchemaInClasspath("jsonschema/webcert-put-notified-utkast-response-schema.json")).extract().response()
                        .as(ListIntygEntry.class);

        assertNotNull(updatedIntyg);

        assertEquals(utkastId, updatedIntyg.getIntygId());
        assertEquals(DEFAULT_PATIENT_PERSONNUMMER, updatedIntyg.getPatientId().getPersonnummer());
        assertEquals("fk7263", updatedIntyg.getIntygType());

        // it's been updated, so version should have been incremented
        assertEquals(1, updatedIntyg.getVersion());

        // and of course it should have been vidarebefordrad as we instructed
        assertTrue(updatedIntyg.isVidarebefordrad());
    }

}
