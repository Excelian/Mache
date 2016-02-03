package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Produces Rabbit MQ event producers and consumers.
 *
 * @param <K> the type of the keys
 */
public class RabbitMQFactory<K> implements MQFactory<K> {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQFactory.class);
    private final Connection connection;
    private final RabbitMQConfig rabbitMqConfig;

    /**
     * Constructor.
     * @param factory - factory
     * @param rabbitMqConfig - rabbitMqConfig
     * @throws JMSException - if an error with jms formats etc.
     * @throws IOException - on error transmitting data
     */
    public RabbitMQFactory(ConnectionFactory factory, RabbitMQConfig rabbitMqConfig) throws JMSException, IOException {
        this.connection = factory.newConnection();
        this.rabbitMqConfig = rabbitMqConfig;
    }

    private Channel createChannel() throws IOException {
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(rabbitMqConfig.getExchangeName(), "direct", true);
        return channel;
    }

    @Override
    public BaseCoordinationEntryEventProducer<K> getProducer(MQConfiguration config)  {
        try {
            return new RabbitMQEventProducer<>(createChannel(), config.getTopicName(), rabbitMqConfig);
        } catch (IOException e) {
            LOG.error("Failed to create Rabbit producer", e);
        }
        return null;
    }

    @Override
    public BaseCoordinationEntryEventConsumer<K> getConsumer(MQConfiguration config) throws IOException, JMSException {
        return new RabbitMQEventConsumer<>(createChannel(), config.getTopicName(), rabbitMqConfig);
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}
