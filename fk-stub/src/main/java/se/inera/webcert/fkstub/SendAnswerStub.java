package se.inera.webcert.fkstub;

import org.w3.wsaddressing10.AttributedURIType;
import se.inera.webcert.sendmedicalcertificateanswer.v1.rivtabp20.SendMedicalCertificateAnswerResponderInterface;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType;
import se.inera.webcert.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType;

/**
 * @author andreaskaltenbach
 */
public class SendAnswerStub implements SendMedicalCertificateAnswerResponderInterface {
    @Override
    public SendMedicalCertificateAnswerResponseType sendMedicalCertificateAnswer(AttributedURIType logicalAddress, SendMedicalCertificateAnswerType parameters) {
        SendMedicalCertificateAnswerResponseType response = new SendMedicalCertificateAnswerResponseType();
        return response;
    }
}
