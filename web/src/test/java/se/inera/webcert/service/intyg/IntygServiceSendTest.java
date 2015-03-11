package se.inera.webcert.service.intyg;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientResponseType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.sendcertificatetorecipient.v1.SendCertificateToRecipientType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.utils.ResultTypeUtil;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType;
import se.inera.webcert.persistence.utkast.model.Omsandning;
import se.inera.webcert.persistence.utkast.model.OmsandningOperation;
import se.inera.webcert.service.exception.WebCertServiceException;
import se.inera.webcert.service.intyg.dto.IntygServiceResult;
import se.inera.webcert.service.log.dto.LogRequest;

@RunWith(MockitoJUnitRunner.class)
public class IntygServiceSendTest extends AbstractIntygServiceTest {

    @Before
    public void setupDefaultAuthorization() {
        when(webCertUserService.isAuthorizedForUnit(anyString(), anyString(), eq(true))).thenReturn(true);
    }

    @Test
    public void testSendIntyg() throws Exception {
        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.okResult());

        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
                .thenReturn(response);

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

        verify(omsandningRepository).save(any(Omsandning.class));
        verify(omsandningRepository).delete(any(Omsandning.class));
        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test
    public void testSendIntygReturnsInfo() throws Exception {
        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.infoResult("Info text"));

        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
                .thenReturn(response);

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.OK, res);

        verify(omsandningRepository).save(any(Omsandning.class));
        verify(omsandningRepository).delete(any(Omsandning.class));
        verify(logService).logSendIntygToRecipient(any(LogRequest.class));
        verify(sendService).sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class));
    }

    @Test
    public void testSendIntygFailingWithError() throws Exception {

        SendCertificateToRecipientResponseType response = new SendCertificateToRecipientResponseType();
        response.setResult(ResultTypeUtil.errorResult(ErrorIdType.APPLICATION_ERROR, "Error text"));
        
        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
        .thenReturn(response);

        Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, INTYG_TYP_FK);
        omsandning.setConfiguration(CONFIG_AS_JSON);

        IntygServiceResult res = intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
        assertEquals(IntygServiceResult.RESCHEDULED, res);

        verify(omsandningRepository, times(2)).save(any(Omsandning.class));
    }

    @Test
    public void testSendIntygFailingWithRuntimeException() throws Exception {

        when(sendService.sendCertificateToRecipient(anyString(), any(SendCertificateToRecipientType.class)))
                .thenThrow(new RuntimeException("A runtime exception"));

        Omsandning omsandning = new Omsandning(OmsandningOperation.SEND_INTYG, INTYG_ID, INTYG_TYP_FK);
        omsandning.setConfiguration(CONFIG_AS_JSON);

        try {
            intygService.sendIntyg(INTYG_ID, INTYG_TYP_FK, "FK", true);
            Assert.fail("WebCertServiceException expected");
        } catch (WebCertServiceException e) {
            // Expected
        }

        verify(omsandningRepository, times(1)).save(any(Omsandning.class));
        verify(omsandningRepository, times(1)).delete(any(Omsandning.class));
    }

}
