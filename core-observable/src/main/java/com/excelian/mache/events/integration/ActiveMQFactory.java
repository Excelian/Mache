package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.MQFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.events.MQConfiguration;

import javax.cache.CacheException;
import javax.jms.Connection;
import javax.jms.JMSException;

public class ActiveMQFactory implements MQFactory {
    private final Connection connection;

    public ActiveMQFactory(final String connectionString) throws JMSException {
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionString);
        connection = connectionFactory.createConnection();
        connection.start();
    }

    @Override
    public BaseCoordinationEntryEventProducer getProducer(final MQConfiguration config) {
        try {
            return new ActiveMQEventProducer(connection, config.getTopicName());
        } catch (JMSException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public BaseCoordinationEntryEventConsumer getConsumer(final MQConfiguration config) {
        try {
            final BaseCoordinationEntryEventConsumer consumer = new ActiveMQEventConsumer(connection, config.getTopicName());
            return consumer;
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