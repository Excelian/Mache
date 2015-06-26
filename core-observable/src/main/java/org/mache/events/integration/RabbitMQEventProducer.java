package org.mache.events.integration;

import java.io.IOException;

import org.mache.coordination.CoordinationEntryEvent;
import org.mache.events.BaseCoordinationEntryEventProducer;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

public class RabbitMQEventProducer<K,V extends CoordinationEntryEvent<K>> extends BaseCoordinationEntryEventProducer<K,V> {

    private Channel channel;
    private String exchangeName;

    public RabbitMQEventProducer(Channel channel, String exchangeName, String topic)
    {
        super(topic);

        this.channel = channel;
        this.exchangeName = exchangeName;
    }

    @Override
    public void send(V event) throws InterruptedException, IOException {
        Gson gson = new Gson();
        System.out.println(super.getTopicName()+" [RabbitMQEventProducer"+ Thread.currentThread().getId()+"] Message:" + event.getUniqueId());
        channel.basicPublish(exchangeName, getTopicName(), true, MessageProperties.PERSISTENT_TEXT_PLAIN, gson.toJson(event).getBytes());
    }

    @Override
    public void close() {
    }

}
