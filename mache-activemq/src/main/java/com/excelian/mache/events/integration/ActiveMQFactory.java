package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.cache.CacheException;
import javax.jms.Connection;
import javax.jms.JMSException;

public class ActiveMQFactory<K> implements MQFactory<K> {
    private final Connection connection;
    private final ActiveMqConfig activeMqConfig;

    public ActiveMQFactory(final String connectionString, ActiveMqConfig activeMqConfig) throws JMSException {
        this.activeMqConfig = activeMqConfig;
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionString);
        connection = connectionFactory.createConnection();
        connection.start();
    }

    @Override
    public BaseCoordinationEntryEventProducer<K> getProducer(final MQConfiguration config) {
        try {
            return new ActiveMQEventProducer<>(connection, config.getTopicName(), activeMqConfig);
        } catch (JMSException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public BaseCoordinationEntryEventConsumer<K> getConsumer(final MQConfiguration config) {
        try {
            return new ActiveMQEventConsumer<>(connection, config.getTopicName(), activeMqConfig);
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
