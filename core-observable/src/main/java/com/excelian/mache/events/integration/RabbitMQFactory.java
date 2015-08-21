package com.excelian.mache.events.integration;

import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.MQFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.events.MQConfiguration;

import javax.jms.JMSException;
import java.io.IOException;

public class RabbitMQFactory<K, V extends CoordinationEntryEvent<K>> implements MQFactory {

    private final static String EXCHANGE_NAME = "coherence-killer-exchange";

    private final Channel channel;
    private final Connection connection;

    public RabbitMQFactory(String connectionString) throws JMSException, IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(1000);
        factory.setHost(connectionString);

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "direct", true);
    }

    @Override
    public BaseCoordinationEntryEventProducer getProducer(MQConfiguration config) {
        BaseCoordinationEntryEventProducer producer = new RabbitMQEventProducer(channel, EXCHANGE_NAME, config.getTopicName());
        return producer;
    }

    @Override
    public BaseCoordinationEntryEventConsumer getConsumer(MQConfiguration config) throws IOException, JMSException {
        BaseCoordinationEntryEventConsumer consumer = new RabbitMQEventConsumer(channel, EXCHANGE_NAME, config.getTopicName());
        return consumer;
    }

    @Override
    public void close() throws IOException {
        channel.close();
        connection.close();
    }
}
