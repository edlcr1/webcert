package se.inera.webcert.web.controller.integration;

import static se.inera.certificate.common.enumerations.CertificateTypes.FK7263;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.inera.webcert.common.security.authority.UserRole;
import se.inera.webcert.service.user.dto.WebCertUser;

/**
 * Controller to enable an external user to access certificates directly from a
 * link in an external patient care system.
 *
 * @author nikpet
 */
@Path("/certificate")
public class LegacyIntygIntegrationController extends BaseIntegrationController {

    private static final String PARAM_CERT_TYPE = "certType";
    private static final String PARAM_CERT_ID = "certId";

    private static final Logger LOG = LoggerFactory.getLogger(LegacyIntygIntegrationController.class);

    private static final String[] GRANTED_ROLES = new String[] { UserRole.ROLE_LAKARE_UTHOPP.name(), UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name() };

    private String urlFragmentTemplate;



    @Override
    protected String[] getGrantedRoles() {
        return GRANTED_ROLES;
    }

    /**
     * Fetches a certificate from IT and then performs a redirect to the view that displays
     * the certificate. Can be used for all types of certificates.
     *
     * @param uriInfo
     * @param intygId
     *            The id of the certificate to view.
     * @return
     */
    @GET
    @Path("/{intygId}/questions")
    public Response redirectToIntyg(@Context UriInfo uriInfo, @PathParam("intygId") String intygId) {

        boolean ok = super.validateRedirectToIntyg(intygId);
        if (!ok) {
            return Response.serverError().build();
        }

        String intygType = FK7263.toString();
        LOG.debug("Redirecting to view intyg {} of type {}", intygId, intygType);

        return buildRedirectResponse(uriInfo, intygType, intygId);
    }

    public void setUrlFragmentTemplate(String urlFragmentTemplate) {
        this.urlFragmentTemplate = urlFragmentTemplate;
    }


    // - - - - - Default scope - - - - -

    private Response buildRedirectResponse(UriInfo uriInfo, String certificateType, String certificateId) {

        UriBuilder uriBuilder = uriInfo.getBaseUriBuilder();

        Map<String, Object> urlParams = new HashMap<String, Object>();
        urlParams.put(PARAM_CERT_TYPE, certificateType);
        urlParams.put(PARAM_CERT_ID, certificateId);

        URI location = uriBuilder.replacePath(getUrlBaseTemplate()).fragment(urlFragmentTemplate).buildFromMap(urlParams);

        return Response.status(Status.TEMPORARY_REDIRECT).location(location).build();
    }

    @Override
    protected void updateUserRoles(WebCertUser user) {
        String userRole = UserRole.ROLE_VARDADMINISTRATOR_UTHOPP.name();

        if (user.hasRole(UserRole.ROLE_LAKARE.name())) {
            userRole = UserRole.ROLE_LAKARE_UTHOPP.name();
        }
        super.writeUserRoles(userRole);
    }

}
