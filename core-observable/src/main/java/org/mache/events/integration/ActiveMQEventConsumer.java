package org.mache.events.integration;

import com.google.gson.Gson;

import javax.jms.*;

import org.mache.coordination.CoordinationEntryEvent;
import org.mache.events.BaseCoordinationEntryEventConsumer;

import java.util.concurrent.*;

public class ActiveMQEventConsumer<K,V extends CoordinationEntryEvent<K>> extends BaseCoordinationEntryEventConsumer<K, V> {

    private Session session;
    private MessageConsumer consumer;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> task;

    public ActiveMQEventConsumer(final Connection connection, final String producerTopicName) throws JMSException {
        super(producerTopicName);
        session=connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createTopic(getTopicName());
        this.consumer = session.createConsumer(destination);
    }

    @Override
    public void beginSubscriptionThread() throws InterruptedException, JMSException {

        task = executor.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        TextMessage message = (TextMessage) consumer.receive();

                        if (message != null) {
                            System.out.println("[ActiveMQEventConsumer"+ Thread.currentThread().getId()+"] Received Message:" + message.getText());

                            Gson gson = new Gson();
                            @SuppressWarnings("unchecked")
							final V event = (V)gson.fromJson(message.getText(), CoordinationEntryEvent.class);

                            RouteEventToListeners(eventMap, event);
                        }
                    }
                    catch (Exception e) {
                        System.out.println("[ActiveMQEventConsumer"+ Thread.currentThread().getId()+"] eventConsumer - could not 'take' event. " + e.getMessage());
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void close() {

        System.out.println("[ActiveMQEventConsumer] Closing");

        try {
            if (task != null) task.cancel(true);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("[ActiveMQEventConsumer] " + e.getMessage());
        }

        try {
            session.close();
        } catch (JMSException e) {
            e.printStackTrace();
            System.out.println("[ActiveMQEventConsumer] " + e.getMessage());
        }

        session=null;
    }
}