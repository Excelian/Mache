package com.excelian.mache.events.integration;

import com.google.gson.Gson;

import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Sends events over a kafka message queue.
 * @param <K> the type of the key
 */
public class KafkaEventProducer<K> extends BaseCoordinationEntryEventProducer<K> {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaEventProducer.class);
    private final Gson gson = new Gson();
    private final Producer<String, String> producer;

    public KafkaEventProducer(final Properties producerConfig, final String topicName) {
        super(topicName);
        producer = new Producer<>(new ProducerConfig(producerConfig));
    }

    @Override
    public void send(final CoordinationEntryEvent<K> event) {
        final String topic = getTopicName().replace("$", ".");

        KeyedMessage<String, String> data = new KeyedMessage<>(topic, "0", gson.toJson(event));
        producer.send(data);
        LOG.info("Sent message {}", data);
    }

    @Override
    public void close() {
        producer.close();
    }
}
