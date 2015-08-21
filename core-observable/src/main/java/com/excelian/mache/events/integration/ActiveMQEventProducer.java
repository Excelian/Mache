package com.excelian.mache.events.integration;

import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.google.gson.Gson;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class ActiveMQEventProducer extends BaseCoordinationEntryEventProducer {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQEventProducer.class);
    Session session;
    MessageProducer producer;

    protected ActiveMQEventProducer(Connection connection, String topicName) throws JMSException {
        super(topicName);

        int oneMinuteMSecs = 60000;

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createTopic(getTopicName());

        producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        producer.setTimeToLive(oneMinuteMSecs);
    }

    @Override
    public void send(final CoordinationEntryEvent<?> event) {

        Gson gson = new Gson();
        TextMessage message;
        try {
            String payload = gson.toJson(event);
            message = session.createTextMessage(payload);
            producer.send(message);
            LOG.debug("SEND: {}", payload);
        } catch (JMSException e) {
            LOG.error("Error sending message: {}", e);
            throw new RuntimeException("Error while sending event", e);
        }
    }

    @Override
    public void close() {
        if (producer != null) {
            try {
                producer.close();
                session.close();
            } catch (JMSException e) {
                // ignored
            }
            producer = null;
        }
    }
}