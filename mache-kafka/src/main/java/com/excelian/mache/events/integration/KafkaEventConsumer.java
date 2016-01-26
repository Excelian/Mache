package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.google.gson.Gson;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Listens to events from Kafka.
 *
 * @param <K> The type of the keys
 */
public class KafkaEventConsumer<K> extends BaseCoordinationEntryEventConsumer<K> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventConsumer.class);
    private final KafkaMqConfig config;
    ConsumerConnector consumer;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    volatile boolean taskStarted = false;
    private Future<?> task;

    /**
     * Constructor.
     *
     * @param consumerConfig properties to configure the kafka endpoint
     * @param topicName      the topic on which to subscribe
     * @param kafkaConfig    the config for kafka itself
     */
    public KafkaEventConsumer(Properties consumerConfig, String topicName, KafkaMqConfig kafkaConfig) {
        super(topicName);
        this.config = kafkaConfig;

        final String consumerGroup = getUniqueConsumerGroupName();
        consumerConfig.put("group.id", consumerGroup);
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(new ConsumerConfig(consumerConfig));

        LOG.info("[KafkaEventConsumer {}] Created consumer with props : {}",
            Thread.currentThread().getId(), consumerConfig);
    }

    private static String getUniqueConsumerGroupName() {
        return java.util.UUID.randomUUID().toString();
    }

    @Override
    public void beginSubscriptionThread() throws InterruptedException, IOException {

        final String topic = getTopicName().replace("$", ".");

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap =
            consumer.createMessageStreams(topicCountMap);
        final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
        final KafkaStream<byte[], byte[]> stream = streams.get(0);

        taskStarted = false;

        task = executor.submit((Runnable) () -> {
                LOG.info("[KafkaEventConsumer{}] Signed up for topic : {} stream - {}",
                    Thread.currentThread().getId(), topic, stream);

                ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
                taskStarted = true;
                while (iterator.hasNext()) {
                    MessageAndMetadata<byte[], byte[]> next = iterator.next();

                    if (next.message() != null) {
                        String message = new String(next.message());
                        LOG.info("[KafkaEventConsumer{}] Received Message: {}",
                            Thread.currentThread().getId(), message);

                        Gson gson = new Gson();
                        @SuppressWarnings("unchecked")
                        final CoordinationEntryEvent<K> event = gson.fromJson(message, CoordinationEntryEvent.class);
                        routeEventToListeners(event);
                    }
                }
            });

        while (!taskStarted) {
            Thread.sleep(1);
        }
    }

    @Override
    public void close() {
        try {
            if (task != null) {
                task.cancel(true);
            }
            executor.shutdown();
            executor.awaitTermination(config.getShutdownTimeoutSeconds(), SECONDS);
        } catch (InterruptedException e) {
            // ignored
        }

        if (consumer != null) {
            consumer.shutdown();
        }
    }
}
