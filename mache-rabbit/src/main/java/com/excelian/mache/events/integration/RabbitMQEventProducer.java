package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RabbitMQEventProducer extends BaseCoordinationEntryEventProducer {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQEventProducer.class);
    private Channel channel;
    private String exchangeName;
    private final RabbitMqConfig rabbitMqConfig;

    public RabbitMQEventProducer(Channel channel, String topic, RabbitMqConfig rabbitMqConfig) {
        super(topic);
        this.channel = channel;
        this.exchangeName = rabbitMqConfig.getExchangeName();
        this.rabbitMqConfig = rabbitMqConfig;
    }

    @Override
    public void send(CoordinationEntryEvent<?> event) {
        Gson gson = new Gson();
        LOG.trace("{} [RabbitMQEventProducer {}] Message: {}", super.getTopicName(), Thread.currentThread().getId(), event.getUniqueId());
        try {
            channel.basicPublish(exchangeName, getTopicName(), true, rabbitMqConfig.getRoutingHeader(), gson.toJson(event).getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error while sending message to rabbitmq", e);
        }
    }

    @Override
    public void close() {
    }

}
