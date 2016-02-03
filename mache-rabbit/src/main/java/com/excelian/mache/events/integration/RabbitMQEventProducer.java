package com.excelian.mache.events.integration;

import com.google.gson.Gson;

import com.excelian.mache.events.BaseCoordinationEntryEventProducer;

import com.excelian.mache.observable.coordination.CoordinationEntryEvent;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Sends events to Rabbit MQ.
 *
 * @param <K> the type of the keys
 */
public class RabbitMQEventProducer<K> extends BaseCoordinationEntryEventProducer<K> {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQEventProducer.class);
    private Channel channel;
    private String exchangeName;
    private final RabbitMQConfig rabbitMqConfig;

    /**
     * Constructor.
     * @param channel - channel
     * @param topic - topic
     * @param rabbitMqConfig - rabbitMqConfig
     */
    public RabbitMQEventProducer(Channel channel, String topic, RabbitMQConfig rabbitMqConfig) {
        super(topic);
        this.channel = channel;
        this.exchangeName = rabbitMqConfig.getExchangeName();
        this.rabbitMqConfig = rabbitMqConfig;
    }

    @Override
    public void send(CoordinationEntryEvent<K> event) {
        Gson gson = new Gson();
        LOG.trace("{} [RabbitMQEventProducer {}] Message: {}", super.getTopicName(),
            Thread.currentThread().getId(), event.getUniqueId());
        try {
            channel.basicPublish(exchangeName, getTopicName(), true,
                rabbitMqConfig.getRoutingHeader(), gson.toJson(event).getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error while sending message to rabbitmq", e);
        }
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            LOG.error("Failed to close channel", e);
        }
    }
}
