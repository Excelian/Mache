package org.mache.events.integration;

import javax.cache.CacheException;
import javax.jms.Connection;
import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.mache.events.BaseCoordinationEntryEventConsumer;
import org.mache.events.BaseCoordinationEntryEventProducer;
import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;

/**
 * Created by sundance on 14/03/15.
 */
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
        }catch(JMSException e){
            throw new CacheException(e);
        }
    }

    @Override
    public void close()
    {
        try {
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
