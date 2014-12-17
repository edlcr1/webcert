package se.inera.webcert.notifications.service;

import org.apache.camel.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponderInterface;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.CertificateStatusUpdateForCareType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultType;
import se.inera.webcert.notifications.routes.RouteHeaders;
import se.inera.webcert.notifications.service.exception.CertificateStatusUpdateServiceException;
import se.inera.webcert.notifications.service.exception.NonRecoverableCertificateStatusUpdateServiceException;

public class CertificateStatusUpdateServiceImpl implements CertificateStatusUpdateService {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateStatusUpdateServiceImpl.class);

    @Autowired
    private CertificateStatusUpdateForCareResponderInterface statusUpdateForCareClient;

    /*
     * (non-Javadoc)
     * 
     * @see se.inera.webcert.notifications.service.CertificateStatusUpdateService#sendStatusUpdate(java.lang.String,
     * se.inera.certificate.clinicalprocess.healthcond.certificate.certificatestatusupdateforcareresponder.v1.
     * CertificateStatusUpdateForCareType)
     */
    @Override
    public void sendStatusUpdate(@Header(RouteHeaders.INTYGS_ID) String intygsId, CertificateStatusUpdateForCareType request,
            @Header(RouteHeaders.LOGISK_ADRESS) String logicalAddress) throws Exception {

        LOG.debug("Sending status update to '{}' for intyg '{}'", logicalAddress, intygsId);

        CertificateStatusUpdateForCareResponseType response = null;

        try {
            response = statusUpdateForCareClient.certificateStatusUpdateForCare(logicalAddress, request);
        } catch (Exception e) {
            LOG.error("Exception occured when sending status update", e);
            throw e;
        }

        ResultType result = response.getResult();
        switch (result.getResultCode()) {
        case ERROR:
            if (result.getErrorId().equals(ErrorIdType.TECHNICAL_ERROR)) {
                throw new NonRecoverableCertificateStatusUpdateServiceException(String.format(
                        "CertificateStatusUpdateServiceImpl failed with non-recoverable error code: %s and message %s", result.getErrorId(),
                        result.getResultText()));
            } else {
                throw new CertificateStatusUpdateServiceException(String.format(
                        "CertificateStatusUpdateServiceImpl failed with error code: %s and message %s", result.getErrorId(), result.getResultText()));
            }
        case INFO:
            LOG.info("CertificateStatusUpdateServiceImpl got message:" + result.getResultText());
            break;
        case OK:
            break;
        default:
            // This should never happen.
            throw new CertificateStatusUpdateServiceException();
        }

    }
}
