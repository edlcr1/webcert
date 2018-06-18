/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
package se.inera.intyg.webcert.web.service.underskrift.fake;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.xmldsig.service.FakeSignatureServiceImpl;
import se.inera.intyg.webcert.persistence.utkast.model.Utkast;
import se.inera.intyg.webcert.web.service.monitoring.MonitoringLogService;
import se.inera.intyg.webcert.web.service.underskrift.BaseXMLSignatureService;
import se.inera.intyg.webcert.web.service.underskrift.model.SignaturBiljett;
import se.inera.intyg.webcert.web.service.underskrift.tracker.RedisTicketTracker;
import se.inera.intyg.webcert.web.service.user.dto.WebCertUser;

import java.nio.charset.Charset;
import java.util.Base64;

@Service
public class FakeUnderskriftServiceImpl extends BaseXMLSignatureService implements FakeUnderskriftService {

    // X509Certificate. Only use for fake!!!
    private static final String FAKE_CERT = "MIIB+zCCAWQCCQCUxqAHHrhg+jANBgkqhkiG9w0BAQsFADBCMQswCQYDVQQGEwJTRTELMAkGA1UE"
            + "CAwCVkcxEzARBgNVBAcMCkdvdGhlbmJ1cmcxETAPBgNVBAoMCENhbGxpc3RhMB4XDTE4MDMxMDIw"
            + "MDY0MFoXDTIxMTIwNDIwMDY0MFowQjELMAkGA1UEBhMCU0UxCzAJBgNVBAgMAlZHMRMwEQYDVQQH"
            + "DApHb3RoZW5idXJnMREwDwYDVQQKDAhDYWxsaXN0YTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkC"
            + "gYEA4cB6VC0f9ne0UKC/XzsoP5ocv7WyGt5378f/DGnVAF3aWzderzLnXMqSdGbLOuEzUUdbjYgQ"
            + "kqQSs6wy872KLf0RzQzllxwpBQJ/2r+CrW6tROJa0FYEIhgWDdRGlS+9+hd3E9Ilz2PTZDF4c1C+"
            + "4l/xq149OCgiAGfadeBZA5MCAwEAATANBgkqhkiG9w0BAQsFAAOBgQDU+Mrw98Qm8K0U8A208Ee0"
            + "1PZeIpqC9CIRIXJd0PFwXJjTlGIWckwrdsgbGtwOAlA2rzAx/FUhQD4/1F4G5mo/DrtOzzx9fKE0"
            + "+MQreTC/HOm61ja3cWm4yI5G0W7bLTBBhsEoOzclycNK/QjeP+wYO+k11mtPM4SP4kCj3gh97g==";

    @Autowired
    private FakeSignatureServiceImpl fakeSignatureService;

    @Autowired
    private RedisTicketTracker redisTicketTracker;

    @Autowired
    private MonitoringLogService monitoringLogService;

    @Override
    public SignaturBiljett finalizeFakeSignature(String ticketId, Utkast utkast, WebCertUser user) {

        SignaturBiljett biljett = redisTicketTracker.findBiljett(ticketId);
        if (biljett == null) {
            throw new RuntimeException("No biljett found in Redis for " + ticketId);
        }

        // We fake a signature here so stuff can validate.

        // Encode the <SignedInfo>...</SignedInfo> into a Base64 string.
        String base64EncodedSignedInfoXml = Base64.getEncoder()
                .encodeToString(biljett.getIntygSignature().getSigningData().getBytes(Charset.forName("UTF-8")));
        String fakeSignatureData = fakeSignatureService.createSignature(base64EncodedSignedInfoXml);

        monitoringLogService.logIntygSigned(utkast.getIntygsId(), utkast.getIntygsTyp(), user.getHsaId(), user.getAuthenticationScheme(),
                utkast.getRelationKod());

        biljett = finalizeXMLDSigSignature(FAKE_CERT, user, biljett, Base64.getDecoder().decode(fakeSignatureData), utkast);
        return redisTicketTracker.updateStatus(biljett.getTicketId(), biljett.getStatus());
    }
}