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

package se.inera.intyg.webcert.kundportalenstub.api;

import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import se.inera.intyg.webcert.kundportalenstub.service.KundportalenStubSettingsApiService;

@Controller
@Path("/settings")
public class KundportalenStubSettingsApi {

    @Autowired
    private KundportalenStubSettingsApiService stubSettingsService;

    @GET
    @Path("/set/{returnValue}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setReturnValue(@PathParam("returnValue") String returnValue) {
        if ("true".equals(returnValue) || "false".equals(returnValue)) {
            stubSettingsService.setSubscriptionReturnValue("true".equals(returnValue));
            return Response.ok("Set stub return value to '" + returnValue + "' and cleared all active subscriptions.").build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("Accepted parameter values are 'true' or 'false'.").build();
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Boolean getReturnValue() {
        return stubSettingsService.getSubscriptionReturnValue();
    }

    @GET
    @Path("/setactive/{orgNumber}/{serviceCode}")
    @Produces(MediaType.APPLICATION_JSON)
    public String setActiveSubscription(@PathParam("orgNumber") String orgNumber, @PathParam("serviceCode") String serviceCode) {
        stubSettingsService.setActiveSubscription(orgNumber, serviceCode);
        return "Set subscription active for organization '" + orgNumber + "' and serviceCode '" + serviceCode + "'.";
    }

    @GET
    @Path("/removeactive/{orgNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public String removeActiveSubscriptions(@PathParam("orgNumber") String orgNumber) {
        stubSettingsService.removeActiveSubscriptions(orgNumber);
        return "Removed active subscription for organization '" + orgNumber + "'.";
    }

    @GET
    @Path("/clearactive")
    @Produces(MediaType.APPLICATION_JSON)
    public String clearActiveSubscriptions() {
        stubSettingsService.clearActiveSubscriptions();
        return "Cleared active subscription for all organizations.";
    }

    @GET
    @Path("/getactive")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getActiveSubscriptions() {
        return stubSettingsService.getActiveSubscriptions();
    }
}
