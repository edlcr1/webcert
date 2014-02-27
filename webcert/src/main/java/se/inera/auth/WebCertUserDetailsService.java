package se.inera.auth;

import java.util.List;

import static se.inera.webcert.hsa.stub.Medarbetaruppdrag.VARD_OCH_BEHANDLING;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import se.inera.auth.exceptions.MissingMedarbetaruppdragException;
import se.inera.webcert.hsa.model.SelectableVardenhet;
import se.inera.webcert.hsa.model.Vardgivare;
import se.inera.webcert.hsa.model.WebCertUser;
import se.inera.webcert.hsa.services.HsaOrganizationsService;

/**
 * @author andreaskaltenbach
 */
public class WebCertUserDetailsService implements SAMLUserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(WebCertUserDetailsService.class);

    private static final String LAKARE = "Läkare";
    private static final String LAKARE_CODE = "204010";

    @Autowired
    private HsaOrganizationsService hsaOrganizationsService;

    @Override
    public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
        LOG.info("User authentication was successful. SAML credential is " + credential);

        SakerhetstjanstAssertion assertion = new SakerhetstjanstAssertion(credential.getAuthenticationAssertion());

        WebCertUser webCertUser = createWebCertUser(assertion);

        // if user has authenticated with other contract than 'Vård och behandling', we have to reject her
        if (!VARD_OCH_BEHANDLING.equals(assertion.getMedarbetaruppdragType())) {
            throw new MissingMedarbetaruppdragException(webCertUser.getHsaId());
        }

        List<Vardgivare> authorizedVardgivare = hsaOrganizationsService.getAuthorizedEnheterForHosPerson(webCertUser.getHsaId());

        // if user does not have access to any vardgivare, we have to reject authentication
        if (authorizedVardgivare.isEmpty()) {
            throw new MissingMedarbetaruppdragException(webCertUser.getHsaId());
        }

        webCertUser.setVardgivare(authorizedVardgivare);
        
        setDefaultSelectedVardenhetOnUser(webCertUser, assertion);
        
        return webCertUser;
    }

    private WebCertUser createWebCertUser(SakerhetstjanstAssertion assertion) {
        WebCertUser webcertUser = new WebCertUser();
        webcertUser.setHsaId(assertion.getHsaId());
        webcertUser.setNamn(assertion.getFornamn() + " " + assertion.getMellanOchEfternamn());
        webcertUser.setForskrivarkod(assertion.getForskrivarkod());
        webcertUser.setAuthenticationScheme(assertion.getAuthenticationScheme());
    
        // lakare flag is calculated by checking for lakare profession in title and title code
        webcertUser.setLakare(LAKARE.equals(assertion.getTitel()) || LAKARE_CODE.equals(assertion.getTitelKod()));
        
        return webcertUser;
    }

    private void setDefaultSelectedVardenhetOnUser(WebCertUser webCertUser, SakerhetstjanstAssertion assertion) {
        
        String enhetHsaIdFromAssertion = assertion.getEnhetHsaId();
        
        SelectableVardenhet defaultVardenhet = webCertUser.findSelectableVardenhet(enhetHsaIdFromAssertion);
        
        if (defaultVardenhet == null) {
            LOG.error("When logging in user '{}', unit with HSA-id {} could not be found in users MIUs", webCertUser.getHsaId(), enhetHsaIdFromAssertion);
            throw new MissingMedarbetaruppdragException(webCertUser.getHsaId());
        }
        
        LOG.debug("Setting care unit '{}' as default unit on user '{}'", defaultVardenhet.getId(), webCertUser.getHsaId());
        
        webCertUser.setValdVardenhet(defaultVardenhet);
    }
}
