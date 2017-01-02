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
package se.inera.intyg.webcert.web.integration.integrationtest;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.matcher.RestAssuredMatchers.matchesXsd;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.stringtemplate.v4.*;

import com.google.common.collect.ImmutableMap;

import se.riv.clinicalprocess.healthcond.certificate.v2.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v2.ResultCodeType;

/**
 * Created by marced on 2016-06-01.
 */
public class SendMessageToCareIT extends BaseWSIntegrationTest {

    private static final String BASE = "Envelope.Body.SendMessageToCareResponse.";
    private static final String SEND_MESSAGE_TO_CARE_V1_0 = "services/send-message-to-care/v1.0";

    private ST requestTemplate;
    private STGroup templateGroup;
    private InputStream xsdInputstream;
    BodyExtractorFilter responseBodyExtractorFilter;

    @Before
    public void setup() throws IOException {
        // Setup String template resource
        templateGroup = new STGroupFile("integrationtestTemplates/sentMessageToCare.v1.stg");
        requestTemplate = templateGroup.getInstanceOf("request");

        xsdInputstream = ClasspathSchemaResourceResolver.load("interactions/SendMessageToCareInteraction/SendMessageToCareResponder_1.0.xsd");

        // We want to validate against the body of the response, and not the entire soap response. This filter will
        // extract that for us.
        responseBodyExtractorFilter = new BodyExtractorFilter(
                ImmutableMap.of("lc", "urn:riv:clinicalprocess:healthcond:certificate:SendMessageToCareResponder:1"),
                "soap:Envelope/soap:Body/lc:SendMessageToCareResponse");
    }

    @Test
    public void testResponseRespectsSchema() throws Exception {

        String enhetsId = "123456";
        String intygsId = "intyg-1";

        requestTemplate.add("data", new ArendeData(intygsId, "KOMPL", "191212121212", enhetsId));

        given().filter(responseBodyExtractorFilter).body(requestTemplate.render())
                .when()
                .post(SEND_MESSAGE_TO_CARE_V1_0)
                .then()
                .body(matchesXsd(xsdInputstream).with(new ClasspathSchemaResourceResolver()));
    }

    @Test
    public void testMessageForNonExistantCertificateFaildWithValidationError() throws Exception {
        String enhetsId = "123456";
        String intygsId = "intyg-nonexistant";
        requestTemplate.add("data", new ArendeData(intygsId, "KOMPL", "191212121212", enhetsId));

        given().body(requestTemplate.render()).when()
                .post(SEND_MESSAGE_TO_CARE_V1_0)
                .then().statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.VALIDATION_ERROR.value()));
    }

    @Test
    public void messageNotFollowingXSDFailsWithValidationError() throws Exception {
        String enhetsId = "<root>123456</root>";
        String intygsId = "intyg-1";
        requestTemplate.add("data", new ArendeData(intygsId, "KOMPL", "191212121212", enhetsId));

        given().body(requestTemplate.render()).when()
                .post(SEND_MESSAGE_TO_CARE_V1_0)
                .then().statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.VALIDATION_ERROR.value()));
    }

    /**
     * Check that even when sending invalid request, Soap faults should get transformed to a valid error response
     */
    @Test
    public void testMessageWithInvalidXMLFailsWithApplicationError() {
        ST brokenTemplate = templateGroup.getInstanceOf("brokenrequest");
        given().body(brokenTemplate.render())
                .when()
                .post(SEND_MESSAGE_TO_CARE_V1_0)
                .then()
                .statusCode(200)
                .rootPath(BASE)
                .body("result.resultCode", is(ResultCodeType.ERROR.value()))
                .body("result.errorId", is(ErrorIdType.APPLICATION_ERROR.value()));
    }

    private static class ArendeData {
        public final String intygsId;
        public final String arende;
        public final String personId;
        public final String enhetsId;

        public ArendeData(String intygsId, String arende, String personId, String enhetsId) {
            this.intygsId = intygsId;
            this.arende = arende;
            this.personId = personId;
            this.enhetsId = enhetsId;
        }
    }
}
