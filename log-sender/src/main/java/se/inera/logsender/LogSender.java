package se.inera.logsender;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;
import org.springframework.stereotype.Component;
import se.inera.logsender.exception.LoggtjanstExecutionException;
import se.inera.logsender.messages.AbstractLogMessage;
import se.riv.ehr.log.store.storelog.v1.StoreLogRequestType;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponderInterface;
import se.riv.ehr.log.store.storelog.v1.StoreLogResponseType;
import se.riv.ehr.log.v1.ActivityType;
import se.riv.ehr.log.v1.CareProviderType;
import se.riv.ehr.log.v1.CareUnitType;
import se.riv.ehr.log.v1.LogType;
import se.riv.ehr.log.v1.ResourceType;
import se.riv.ehr.log.v1.ResourcesType;
import se.riv.ehr.log.v1.SystemType;
import se.riv.ehr.log.v1.UserType;

/**
 * @author andreaskaltenbach
 */
@Component
public class LogSender {

    private static final Logger LOG = LoggerFactory.getLogger(LogSender.class);

    @Value("${logsender.bulkSize}")
    private int bulkSize;

    @Autowired
    private StoreLogResponderInterface loggTjanstResponder;

    @Autowired
    @Qualifier("jmsTemplate")
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("nonTransactedJmsTemplate")
    private JmsTemplate nonTransactedJmsTemplate;

    @Autowired
    private Queue queue;

    @PostConstruct
    public void checkConfiguration() {
        if (bulkSize == 0) {
            throw new IllegalStateException("'bulkSize' has to be greater than zero");
        }
    }

    private int bulkSize() {
        return nonTransactedJmsTemplate.execute(new SessionCallback<Integer>() {
            @Override
            public Integer doInJms(Session session) throws JMSException {
                QueueBrowser queueBrowser = session.createBrowser(queue);
                Enumeration queueMessageEnum = queueBrowser.getEnumeration();
                int count = 0;
                while (queueMessageEnum.hasMoreElements() && count < bulkSize) {
                    queueMessageEnum.nextElement();
                    count++;
                }
                return count;
            }
        }, true);
    }

    public void sendLogEntries() {

        final int chunk = bulkSize();

        if (chunk == 0) {
            LOG.info("Zero messages in logging queue. Nothing will be sent to loggtjänst");
        }

        else {
            Boolean reExecute = jmsTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public Boolean doInJms(Session session) throws JMSException {

                    int count = chunk;

                    LOG.info("Transferring " + count + " log entries to loggtjänst.");

                    MessageConsumer consumer = session.createConsumer(queue);

                    List<Message> messages = new ArrayList<>();
                    while (count > 0) {
                        messages.add(consumer.receive());
                        count--;
                    }

                    try {
                        sendLogEntriesToLoggtjanst(convert(messages));
                        session.commit();
                        return true;
                    } catch (LoggtjanstExecutionException e) {
                        LOG.warn("Failed to send log entries to loggtjänst, JMS session will be rolled back.");
                        session.rollback();
                        return false;
                    }
                }
            }, true);

            // there may be messages left on the queue after the first chunk, so reperform the action
            if (reExecute) {
                sendLogEntries();
            }
        }

    }

    private List<LogType> convert(List<Message> messages) {
        List<LogType> logTypes = new ArrayList<>();
        for (Message message : messages) {
            logTypes.add(convert(message));
        }
        return logTypes;
    }

    private LogType convert(Message message) {
        try {
            Object element = ((ObjectMessage) message).getObject();

            if (element instanceof AbstractLogMessage) {
                AbstractLogMessage logMessage = (AbstractLogMessage) element;
                return convert(logMessage);

            } else {
                throw new RuntimeException("Unrecognized message type " + element.getClass().getCanonicalName());
            }

        } catch (JMSException e) {
            throw new RuntimeException("Failed to read incoming JMS message", e);
        }
    }

    private LogType convert(AbstractLogMessage source) {
        LogType logType = new LogType();

        logType.setLogId(source.getLogId());

        SystemType system = new SystemType();
        system.setSystemId(source.getSystemId());
        logType.setSystem(system);

        ActivityType activity = new ActivityType();
        activity.setActivityType(source.getActivityType());
        activity.setStartDate(source.getTimestamp());
        activity.setPurpose(source.getPurpose());
        logType.setActivity(activity);

        UserType user = new UserType();
        user.setUserId(source.getUserId());
        CareProviderType careProvider = new CareProviderType();
        careProvider.setCareProviderId(source.getVardgivareId());
        user.setCareProvider(careProvider);
        CareUnitType careUnit = new CareUnitType();
        careUnit.setCareUnitId(source.getEnhetId());
        user.setCareUnit(careUnit);
        logType.setUser(user);

        logType.setResources(new ResourcesType());
        ResourceType resource = new ResourceType();
        resource.setResourceType(source.getResourceType());
        CareProviderType resourceCareProvider = new CareProviderType();
        resourceCareProvider.setCareProviderId(source.getVardgivareId());
        resource.setCareProvider(resourceCareProvider);
        logType.getResources().getResource().add(resource);

        return logType;
    }

    private void sendLogEntriesToLoggtjanst(List<LogType> logEntries) {

        StoreLogRequestType request = new StoreLogRequestType();
        request.getLog().addAll(logEntries);
        StoreLogResponseType response = loggTjanstResponder.storeLog(null, request);
        switch (response.getResultType().getResultCode()) {
        case OK:
        case INFO:
            break;
        default:
            throw new LoggtjanstExecutionException();
        }
    }
}
