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

package se.inera.intyg.webcert.web.web.controller.testability.facade;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import se.inera.intyg.webcert.web.web.controller.AbstractApiController;
import se.inera.intyg.webcert.web.web.controller.testability.facade.util.CreateCertificateTestabilityUtil;

@Path("/certificate")
public class CertificateTestabilityController extends AbstractApiController {

    @Autowired
    private final CreateCertificateTestabilityUtil createCertificateTestabilityUtil;

    public CertificateTestabilityController(
        CreateCertificateTestabilityUtil createCertificateTestabilityUtil) {
        this.createCertificateTestabilityUtil = createCertificateTestabilityUtil;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON + UTF_8_CHARSET)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Response createCertificate(@RequestBody @NotNull CreateCertificateRequestDTO createCertificateRequest) {
        return Response.ok(createCertificateTestabilityUtil.createNewCertificate(createCertificateRequest)).build();
    }
}
