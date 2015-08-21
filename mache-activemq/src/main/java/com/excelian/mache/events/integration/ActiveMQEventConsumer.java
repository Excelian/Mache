package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class ActiveMQEventConsumer extends BaseCoordinationEntryEventConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQEventConsumer.class);
    private Session session;
    private MessageConsumer consumer;

    ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> task;

    public ActiveMQEventConsumer(final Connection connection, final String producerTopicName) throws JMSException {
        super(producerTopicName);
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
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
                            LOG.info("[ActiveMQEventConsumer {}] Received Message: {}",
                                    Thread.currentThread().getId(), message.getText());

                            final CoordinationEntryEvent<?> event = new Gson().fromJson(message.getText(),
                                    CoordinationEntryEvent.class);

                            routeEventToListeners(eventMap, event);
                        }
                    } catch (JMSException e) {
                        LOG.error("[ActiveMQEventConsumer {}] eventConsumer - could not 'take' event.\\n{}",
                                Thread.currentThread().getId(), e);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void close() {
        LOG.info("[ActiveMQEventConsumer] Closing");

        try {
            if (task != null) {
                task.cancel(true);
            }
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("[ActiveMQEventConsumer] {}", e);
        }

        try {
            session.close();
        } catch (JMSException e) {
            LOG.error("[ActiveMQEventConsumer] ", e);
        }

        session = null;
    }
}