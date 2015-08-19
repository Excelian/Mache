package org.mache.events.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.mache.coordination.CoordinationEntryEvent;
import org.mache.events.BaseCoordinationEntryEventConsumer;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RabbitMQEventConsumer extends BaseCoordinationEntryEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitMQEventConsumer.class);
    private final Channel channel;
    private final String queueName;
    String consumerTag="";

    public RabbitMQEventConsumer(final Channel channel, String exchangeName, String producerTypeName) throws JMSException, IOException {
        super(producerTypeName);

        this.channel = channel;
        Map<String, Object> queueArgs = GetEvictQueueArguments();
        channel.exchangeDeclare(exchangeName, "direct", true);
        queueName = channel.queueDeclare(getUniqueQueueName(), true, false, false, queueArgs).getQueue();
        channel.queueBind(queueName, exchangeName, getTopicName());
    }

    private static String getUniqueQueueName()
    {
        return java.util.UUID.randomUUID().toString();
    }

    private static Map<String, Object> GetEvictQueueArguments() {
        Map<String, Object> queueArgs = new HashMap<String, Object>();
        int oneMinuteMSecs = 60000;
        queueArgs.put("x-message-ttl", oneMinuteMSecs);
        queueArgs.put("x-expires", oneMinuteMSecs);

        queueArgs.put("x-max-length", 10000);
        return queueArgs;
    }

    @Override
    public void beginSubscriptionThread() throws InterruptedException, IOException {
        DefaultConsumer consumer=new DefaultConsumer(channel)
        {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body)
                    throws IOException
            {
                long deliveryTag = envelope.getDeliveryTag();

                LOG.info("[RabbitMQEventConsumer {}] Received Message: {}", Thread.currentThread().getId(), new String(body));

                Gson gson=new Gson();
				final CoordinationEntryEvent<?> event= gson.fromJson(new String(body), CoordinationEntryEvent.class);
                routeEventToListeners(eventMap, event);

                channel.basicAck(deliveryTag, false);
            }
        };

        consumerTag=channel.basicConsume(queueName, false, consumer);
    }

    public void close()
    {
        try {
            if(channel!=null) {
                channel.basicCancel(consumerTag);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
