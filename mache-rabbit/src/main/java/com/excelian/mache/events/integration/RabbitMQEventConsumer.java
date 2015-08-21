package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RabbitMQEventConsumer extends BaseCoordinationEntryEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQEventConsumer.class);
    private final Channel channel;
    private final RabbitMqConfig rabbitMqConfig;
    private final String queueName;
    String consumerTag = "";

    public RabbitMQEventConsumer(final Channel channel, String producerTypeName, RabbitMqConfig rabbitMqConfig) throws JMSException, IOException {
        super(producerTypeName);
        this.channel = channel;
        this.rabbitMqConfig = rabbitMqConfig;
        Map<String, Object> queueArgs = getEvictQueueArguments();
        final String exchangeName = rabbitMqConfig.getExchangeName();
        channel.exchangeDeclare(exchangeName, "direct", true);
        queueName = channel.queueDeclare(getUniqueQueueName(), true, false, false, queueArgs).getQueue();
        channel.queueBind(queueName, exchangeName, getTopicName());
    }

    private String getUniqueQueueName() {
        return java.util.UUID.randomUUID().toString();
    }

    private Map<String, Object> getEvictQueueArguments() {
        Map<String, Object> queueArgs = new HashMap<>();

        queueArgs.put("x-message-ttl", rabbitMqConfig.getMessageTTLMilliSeconds());
        queueArgs.put("x-expires", rabbitMqConfig.getMessageExpiryMilliSeconds());
        queueArgs.put("x-max-length", rabbitMqConfig.getMaxLength());
        return queueArgs;
    }

    @Override
    public void beginSubscriptionThread() throws InterruptedException, IOException {
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body)
                    throws IOException {
                long deliveryTag = envelope.getDeliveryTag();

                LOG.info("[RabbitMQEventConsumer {}] Received Message: {}", Thread.currentThread().getId(), new String(body));

                Gson gson = new Gson();
                final CoordinationEntryEvent<?> event = gson.fromJson(new String(body), CoordinationEntryEvent.class);
                routeEventToListeners(eventMap, event);

                channel.basicAck(deliveryTag, false);
            }
        };
        consumerTag = channel.basicConsume(queueName, false, consumer);
    }

    public void close() {
        try {
            if (channel != null) {
                channel.basicCancel(consumerTag);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
