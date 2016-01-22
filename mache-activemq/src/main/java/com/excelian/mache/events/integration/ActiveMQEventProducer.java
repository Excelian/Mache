package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Send Mache events to Active MQ.
 *
 * @param <K> key of Mache
 */
public class ActiveMQEventProducer<K> extends BaseCoordinationEntryEventProducer<K> {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQEventProducer.class);
    Session session;
    MessageProducer producer;

    protected ActiveMQEventProducer(Connection connection, String topicName, long timeToLiveInMillis, int deliveryMode,
                                    int acknowledgementMode) throws JMSException {
        super(topicName);

        session = connection.createSession(false, acknowledgementMode);
        Destination destination = session.createTopic(getTopicName());

        producer = session.createProducer(destination);
        producer.setDeliveryMode(deliveryMode);
        producer.setTimeToLive(timeToLiveInMillis);
    }

    @Override
    public void send(final CoordinationEntryEvent<K> event) {
        final Gson gson = new Gson();
        try {
            String payload = gson.toJson(event);
            TextMessage message = session.createTextMessage(payload);
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