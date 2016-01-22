package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Consumes Mache events from Active MQ.
 *
 * @param <K> key of Mache
 */
public class ActiveMQEventConsumer<K> extends BaseCoordinationEntryEventConsumer<K> {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQEventConsumer.class);
    ExecutorService executor = Executors.newSingleThreadExecutor();
    private Session session;
    private MessageConsumer consumer;
    private Future<?> task;
    private volatile boolean notStopped = true;

    /**
     * @param connection The ActiveMQ Connection to use.
     * @param producerTopicName The topic name to receive events from.
     * @param acknowledgementMode The JMS acknowledgement mode to use.
     * @throws JMSException If a JMS error occurred.
     */
    public ActiveMQEventConsumer(final Connection connection, final String producerTopicName,
                                 int acknowledgementMode) throws JMSException {
        super(producerTopicName);
        session = connection.createSession(false, acknowledgementMode);
        Destination destination = session.createTopic(getTopicName());
        this.consumer = session.createConsumer(destination);
    }

    @Override
    public void beginSubscriptionThread() throws InterruptedException, JMSException {
        final Type genericType = new TypeToken<CoordinationEntryEvent<K>>() {}.getType();
        final Gson gson = new Gson();

        task = executor.submit(() -> {
                while (notStopped) {
                    try {
                        TextMessage message = (TextMessage) consumer.receive(1);

                        if (message != null) {

                            if (LOG.isDebugEnabled()) {
                                LOG.debug("[ActiveMQEventConsumer {}] Received Message: {}",
                                        Thread.currentThread().getId(), message.getText());
                            }

                            final CoordinationEntryEvent<K> event = gson.fromJson(message.getText(), genericType);

                            routeEventToListeners(event);
                        }
                    } catch (JMSException e) {
                        LOG.error("[ActiveMQEventConsumer {}] eventConsumer - could not 'take' event.\\n{}",
                                Thread.currentThread().getId(), e);
                        throw new RuntimeException(e);
                    }
                }
            });
    }

    @Override
    public void close() {
        LOG.info("[ActiveMQEventConsumer] Closing");
        notStopped = false;
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