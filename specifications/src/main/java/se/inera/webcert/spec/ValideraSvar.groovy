package se.inera.webcert.spec

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource

import se.inera.certificate.spec.util.FitnesseHelper
import se.inera.webcert.receivemedicalcertificateanswer.v1.rivtabp20.ReceiveMedicalCertificateAnswerResponderInterface
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerResponseType
import se.inera.webcert.receivemedicalcertificateanswerresponder.v1.ReceiveMedicalCertificateAnswerType
import se.inera.webcert.spec.util.WsClientFixture

/**
 *
 * @author andreaskaltenbach
 */
class ValideraSvar extends WsClientFixture {

    ReceiveMedicalCertificateAnswerResponderInterface receiveMedicalCertificateAnswerResponder

    public ValideraSvar() {
        super()
    }
    
    public ValideraSvar(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String url = baseUrl + "services/receive-answer/v1.0"
        receiveMedicalCertificateAnswerResponder = createClient(ReceiveMedicalCertificateAnswerResponderInterface.class, url)
    }

    String filnamn
    String vardReferens
    
    ReceiveMedicalCertificateAnswerResponseType response

    public void execute() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(ReceiveMedicalCertificateAnswerType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        ReceiveMedicalCertificateAnswerType request = unmarshaller.unmarshal(new StreamSource(new FileInputStream (FitnesseHelper.getFile(filnamn))),
                                                                        ReceiveMedicalCertificateAnswerType.class).getValue()

        if (vardReferens) request.answer.vardReferensId = vardReferens
        
        response = receiveMedicalCertificateAnswerResponder.receiveMedicalCertificateAnswer(logicalAddress, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
