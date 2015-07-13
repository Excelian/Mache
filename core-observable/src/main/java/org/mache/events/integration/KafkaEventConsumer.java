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
    private Future<?> task;

    public KafkaEventConsumer(Properties consumerConfig, String producerTypeName) {
        super(producerTypeName);

        consumerConfig.put("group.id", getUniqueConsumerGroupName());
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector( new ConsumerConfig(consumerConfig) );
    }

    private static String getUniqueConsumerGroupName()
    {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public void beginSubscriptionThread() throws InterruptedException, IOException {

        String TOPIC = getTopicName().replace("$", ".");

        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(TOPIC, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(TOPIC);

        task = executor.submit(new Runnable() {
            @Override
            public void run() {
                final KafkaStream<byte[], byte[]> stream = streams.get(0);

                ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
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
