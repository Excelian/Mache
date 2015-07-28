package org.mache.events.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;

import org.mache.coordination.CoordinationEntryEvent;
import org.mache.events.BaseCoordinationEntryEventConsumer;

import com.google.gson.Gson;

public class KafkaEventConsumer extends BaseCoordinationEntryEventConsumer {


    ConsumerConnector consumer;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    volatile boolean taskStarted = false;
    private Future<?> task;

    public KafkaEventConsumer(Properties consumerConfig, String producerTypeName) {
        super(producerTypeName);

        final String consumerGroup = getUniqueConsumerGroupName();
		consumerConfig.put("group.id", consumerGroup);
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector( new ConsumerConfig(consumerConfig) );
        
        System.out.println("[KafkaEventConsumer"+ Thread.currentThread().getId()+"] Created consumer with props :" + consumerConfig);
    }

    private static String getUniqueConsumerGroupName()
    {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public void beginSubscriptionThread() throws InterruptedException, IOException {

        final String TOPIC = getTopicName().replace("$", ".");

        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(TOPIC, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(TOPIC);
        final KafkaStream<byte[], byte[]> stream = streams.get(0);
        
        taskStarted = false;

        task = executor.submit(new Runnable() {
            @Override
            public void run() {
            	System.out.println("[KafkaEventConsumer"+ Thread.currentThread().getId()+"] Signed up for topic :" + TOPIC + ", stream - " + stream);

            	ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
                taskStarted = true;
                while(iterator.hasNext()) {
                    MessageAndMetadata<byte[], byte[]> next = iterator.next();

                    if (next.message() != null) {
                        String message = new String(next.message());
                        System.out.println("[KafkaEventConsumer"+ Thread.currentThread().getId()+"] Received Message:" + message);

                        Gson gson = new Gson();
						final CoordinationEntryEvent<?> event = gson.fromJson(message, CoordinationEntryEvent.class);
                        routeEventToListeners(eventMap, event);
                    }
                }
                
            }
        });
        
        while(!taskStarted) {
        	Thread.sleep(1);
        }
    }

    public void close() {

        try {
            if (task != null) task.cancel(true);
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("[KafkaEventConsumer] " + e.getMessage());
        }

        if (consumer != null) {
            consumer.shutdown();
        }
    }
}
