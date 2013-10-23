package se.inera.webcert.intygstjanststub;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import se.inera.certificate.clinicalprocess.healtcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType;

/**
 * @author marced
 */
public class StubRestApi {

    @Autowired
    private IntygStore intygStore;

    @GET
    @Path("/intyg")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<GetCertificateForCareResponseType> getAllIntyg() {
        return intygStore.getAllIntyg().values();
    }

    @GET
    @Path("/intyg/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public GetCertificateForCareResponseType getIntyg(@PathParam("id") String id) {
        return intygStore.getAllIntyg().get(id);
    }

}
