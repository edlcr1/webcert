/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.auth.common;

import static se.inera.intyg.webcert.web.auth.common.AuthConstants.HTTP_ID_SAMBI_SE_LOA_LOA3;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_MOBILE_TWO_FACTOR_CONTRACT;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SMARTCARD_PKI;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI;
import static se.inera.intyg.webcert.web.auth.common.AuthConstants.URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_UNSPECIFIED;

import java.util.Arrays;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;
import se.inera.intyg.webcert.web.auth.WebcertUserDetailsService;
import se.inera.intyg.webcert.web.auth.eleg.ElegWebCertUserDetailsService;

/**
 * Created by eriklupander on 2015-08-12.
 *
 * Facade for our two different UserDetailsServices that looks up authenticated logins over either SITHS or e-leg.
 *
 * Checks the Authentication context class ref to determine method:
 *
 * <li>http://id.sambi.se/loa/loa3 - SITHS</li>
 * <li>urn:oasis:names:tc:SAML:2.0:ac:classes:SoftwarePKI - E-leg</li>
 * <li>urn:oasis:names:tc:SAML:2.0:ac:classes:SmartcardPKI - E-leg</li>
 * <li>urn:oasis:names:tc:SAML:2.0:ac:classes:MobileTwofactorContract - E-leg</li>
 *
 * For testing purposes, this class is also aware of urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified, but will only
 * initiate authorization if the application has the "dev" spring profile active.
 */
@Service
public class UnifiedUserDetailsService implements SAMLUserDetailsService {

    @Resource
    private Environment environment;

    /**
     * User details service for e-leg authenticated private practitioners.
     */
    @Autowired
    private ElegWebCertUserDetailsService elegWebCertUserDetailsService;

    /**
     * User details service for SITHS authenticated personnel.
     */
    @Autowired
    private WebcertUserDetailsService webcertUserDetailsService;

    @Override
    public Object loadUserBySAML(SAMLCredential samlCredential) {
        if (samlCredential.getAuthenticationAssertion() == null) {
            throw new IllegalArgumentException("Cannot determine which underlying UserDetailsService to use for SAMLCredential. "
                + "Must contain an authenticationAssertion");
        }

        String authnContextClassRef = samlCredential.getAuthenticationAssertion().getAuthnStatements().get(0).getAuthnContext()
            .getAuthnContextClassRef().getAuthnContextClassRef();
        if (authnContextClassRef == null || authnContextClassRef.trim().length() == 0) {
            throw new IllegalArgumentException("Cannot determine which underlying UserDetailsService to use for SAMLCredential. "
                + "AuthenticationContextClassRef was null or empty. Should be one of:\n"
                + HTTP_ID_SAMBI_SE_LOA_LOA3 + " or "
                + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI + " or "
                + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_MOBILE_TWO_FACTOR_CONTRACT + " or "
                + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SMARTCARD_PKI);
        }

        switch (authnContextClassRef) {
            case URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_MOBILE_TWO_FACTOR_CONTRACT:
            case URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SMARTCARD_PKI:
            case URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI:
                return elegWebCertUserDetailsService.loadUserBySAML(samlCredential);
            case HTTP_ID_SAMBI_SE_LOA_LOA3:
                return webcertUserDetailsService.loadUserBySAML(samlCredential);
            case URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_UNSPECIFIED:
                if (Arrays.stream(environment.getActiveProfiles()).anyMatch("wc-security-test"::equalsIgnoreCase)) {
                    return webcertUserDetailsService.loadUserBySAML(samlCredential);
                }
                throw new IllegalArgumentException(
                    "AuthorizationContextClassRef " + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_UNSPECIFIED + " is not allowed");
            default:
                throw new IllegalArgumentException("AuthorizationContextClassRef was " + authnContextClassRef + ", expected one of: "
                    + HTTP_ID_SAMBI_SE_LOA_LOA3 + " or "
                    + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SOFTWARE_PKI + " or "
                    + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_MOBILE_TWO_FACTOR_CONTRACT + " or "
                    + URN_OASIS_NAMES_TC_SAML_2_0_AC_CLASSES_SMARTCARD_PKI);
        }

    }
}
