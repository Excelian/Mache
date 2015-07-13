package org.mache.events.integration;

import java.io.IOException;

import org.mache.coordination.CoordinationEntryEvent;
import org.mache.events.BaseCoordinationEntryEventProducer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

public class RabbitMQEventProducer extends BaseCoordinationEntryEventProducer {

    private Channel channel;
    private String exchangeName;

    public RabbitMQEventProducer(Channel channel, String exchangeName, String topic)
    {
        super(topic);

        this.channel = channel;
        this.exchangeName = exchangeName;
    }

    @Override
    public void send(CoordinationEntryEvent<?> event) {
        Gson gson = new Gson();
        System.out.println(super.getTopicName()+" [RabbitMQEventProducer"+ Thread.currentThread().getId()+"] Message:" + event.getUniqueId());
        try {
			channel.basicPublish(exchangeName, getTopicName(), true, MessageProperties.PERSISTENT_TEXT_PLAIN, gson.toJson(event).getBytes());
		} catch (IOException e) {
			throw new RuntimeException("Error while sending message to rabbitmq", e);
		}
    }

    @Override
    public void close() {
    }

}
