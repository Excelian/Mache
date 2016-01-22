package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;

import javax.cache.CacheException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

/**
 * A Factory to create Active MQ Event objects.
 *
 * @param <K> key of Mache
 */
public class ActiveMQFactory<K> implements MQFactory<K> {
    private final Connection connection;
    private long timeToLiveInMillis;
    private int deliveryMode;
    private int acknowledgementMode;

    /**
     * @param connectionFactory The ActiveMQ ConnectionFactory to use.
     * @param timeToLiveInMillis The JMS TTL to use.
     * @param deliveryMode The JMS delivery mode to use.
     * @param acknowledgementMode The JMS acknowledgement mode to use.
     */
    public ActiveMQFactory(ConnectionFactory connectionFactory, long timeToLiveInMillis, int deliveryMode,
                           int acknowledgementMode) {
        this.timeToLiveInMillis = timeToLiveInMillis;
        this.deliveryMode = deliveryMode;
        this.acknowledgementMode = acknowledgementMode;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
        } catch (JMSException exp) {
            throw new RuntimeException("Failed to connect to " + connectionFactory, exp);
        }
    }

    @Override
    public BaseCoordinationEntryEventProducer<K> getProducer(final MQConfiguration config) {
        try {
            return new ActiveMQEventProducer<>(connection, config.getTopicName(), timeToLiveInMillis, deliveryMode,
                    acknowledgementMode);
        } catch (JMSException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public BaseCoordinationEntryEventConsumer<K> getConsumer(final MQConfiguration config) {
        try {
            return new ActiveMQEventConsumer<>(connection, config.getTopicName(), acknowledgementMode);
        } catch (JMSException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
