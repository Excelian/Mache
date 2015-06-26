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

public class RabbitMQEventConsumer<K, V extends CoordinationEntryEvent<K>> extends BaseCoordinationEntryEventConsumer<K, V> {

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
            	//TODO probably to remove
                //String routingKey = envelope.getRoutingKey();
                //String contentType = properties.getContentType();
                long deliveryTag = envelope.getDeliveryTag();

                System.out.println("[RabbitMQEventConsumer"+ Thread.currentThread().getId()+"] Received Message:" + new String(body));

                Gson gson=new Gson();
                @SuppressWarnings("unchecked")
				final V event= (V)gson.fromJson(new String(body), CoordinationEntryEvent.class);

                RouteEventToListeners(eventMap, event);

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
