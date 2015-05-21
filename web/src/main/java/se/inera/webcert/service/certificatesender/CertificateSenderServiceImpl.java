package se.inera.webcert.service.certificatesender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.jms.*;

/**
 * Created by eriklupander on 2015-05-20.
 */
@Component
public class CertificateSenderServiceImpl implements CertificateSenderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateSenderServiceImpl.class);

    // TODO Refactor into webcert-common when it becomes available.
    private static final java.lang.String INTYGS_ID = "INTYGS_ID";
    private static final java.lang.String INTYGS_TYP = "INTYGS_TYP";
    private static final java.lang.String PERSON_ID = "PERSON_ID";
    private static final java.lang.String RECIPIENT = "RECIPIENT";
    private static final java.lang.String LOGICAL_ADDRESS = "LOGICAL_ADDRESS";

    private static final String STORE_MESSAGE = "STORE";
    private static final String SEND_MESSAGE = "SEND";
    private static final String REVOKE_MESSAGE = "REVOKE";
    private static final String MESSAGE_TYPE = "MESSAGE_TYPE";

    @Value("${intygstjanst.logicaladdress}")
    private String logicalAddress;


    @Autowired(required = false)
    @Qualifier("jmsCertificateSenderTemplate")
    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void checkJmsTemplate() {
        if (jmsTemplate == null) {
            LOGGER.error("CertificateSender JMS template is unavailable!");
        }
    }

    @Override
    public void storeCertificate(String intygsId, String intygsTyp, String jsonBody) {
        jmsTemplate.send(new StoreCertificateMessageCreator(intygsId, jsonBody, intygsTyp, logicalAddress));
    }

    @Override
    public void sendCertificate(String intygsId, String personId, String recipientId) {
        jmsTemplate.send(new SendCertificateMessageCreator(intygsId, personId, recipientId, logicalAddress));
    }

    @Override
    public void revokeCertificate(String intygsId, String xmlBody) {
        jmsTemplate.send(new RevokeCertificateMessageCreator(intygsId, xmlBody, logicalAddress));
    }


    static final class StoreCertificateMessageCreator implements MessageCreator {

        private String intygsId;
        private String body;
        private String intygsTyp;
        private String logicalAddress;

        public StoreCertificateMessageCreator(String intygsId, String body, String intygsTyp, String logicalAddress) {
            this.intygsId = intygsId;
            this.body = body;
            this.intygsTyp = intygsTyp;
            this.logicalAddress = logicalAddress;
        }

        public Message createMessage(Session session) throws JMSException {
            Message message = session.createTextMessage(this.body);
            message.setStringProperty("JMSXGroupID", intygsId);
            message.setStringProperty(MESSAGE_TYPE, STORE_MESSAGE);
            message.setStringProperty(INTYGS_TYP, intygsTyp);
            message.setStringProperty(LOGICAL_ADDRESS, logicalAddress);
            return message;
        }
    }

    static final class SendCertificateMessageCreator implements MessageCreator {

        private String intygsId;
        private String personId;
        private String recipientId;
        private String logicalAddress;

        public SendCertificateMessageCreator(String intygsId, String personId, String recipientId, String logicalAddress) {
            this.intygsId = intygsId;
            this.personId = personId;
            this.recipientId = recipientId;
            this.logicalAddress = logicalAddress;
        }

        public Message createMessage(Session session) throws JMSException {
            Message message = session.createTextMessage();
            message.setStringProperty("JMSXGroupID", intygsId);
            message.setStringProperty(MESSAGE_TYPE, SEND_MESSAGE);

            message.setStringProperty(INTYGS_ID, intygsId);
            message.setStringProperty(PERSON_ID, personId);
            message.setStringProperty(RECIPIENT, recipientId);
            message.setStringProperty(LOGICAL_ADDRESS, logicalAddress);
            return message;
        }
    }

    static final class RevokeCertificateMessageCreator implements MessageCreator {
        private final String intygsId;
        private final String xmlBody;
        private final String logicalAddress;

        public RevokeCertificateMessageCreator(String intygsId, String xmlBody, String logicalAddress) {
            this.intygsId = intygsId;
            this.xmlBody = xmlBody;
            this.logicalAddress = logicalAddress;
        }

        @Override
        public Message createMessage(Session session) throws JMSException {
            Message message = session.createTextMessage(xmlBody);
            message.setStringProperty("JMSXGroupID", intygsId);
            message.setStringProperty(MESSAGE_TYPE, REVOKE_MESSAGE);

            message.setStringProperty(INTYGS_ID, intygsId);
            message.setStringProperty(LOGICAL_ADDRESS, logicalAddress);
            return message;
        }
    }
}
