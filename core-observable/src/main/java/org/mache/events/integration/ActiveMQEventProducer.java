package org.mache.events.integration;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.mache.coordination.CoordinationEntryEvent;
import org.mache.events.BaseCoordinationEntryEventProducer;

import com.google.gson.Gson;

public class ActiveMQEventProducer extends BaseCoordinationEntryEventProducer {

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
            System.out.println("SEND:"+payload);
		} catch (JMSException e) {
			System.out.println("Error sending message ");
			e.printStackTrace();
			
			throw new RuntimeException("Error while sending event", e);
		}
    }

    @Override
    public void close() {
        if(producer!=null)
        {
            try {
                producer.close();
                session.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
            producer=null;
        }
    }
}