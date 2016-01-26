package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;

import java.io.IOException;
import java.util.Properties;
import javax.jms.JMSException;

/**
 * Creates config for kafka consumers and producers.
 * @param <K> the type of the keys being stored in mache
 */
public class KafkaMQFactory<K> implements MQFactory<K> {
    private final KafkaMqConfig kafkaMqConfig;

    public KafkaMQFactory(KafkaMqConfig kafkaMqConfig) throws JMSException, IOException {
        this.kafkaMqConfig = kafkaMqConfig;
    }

    Properties createProducerConfig() {
        String zkPort = kafkaMqConfig.getZookeeperProducerPort();
        String zkConnection = kafkaMqConfig.getZookeeperConnectionString(zkPort);

        Properties producerProperties = new Properties();
        producerProperties.put("metadata.broker.list", zkConnection);
        producerProperties.put("serializer.class", kafkaMqConfig.getSerializerClassName());
        producerProperties.put("request.required.acks", kafkaMqConfig.getRequiredAcks());

        return producerProperties;
    }

    Properties createConsumerConfig() {
        String zkPort = kafkaMqConfig.getZookeeperConsumerPort();
        String zkConnection = kafkaMqConfig.getZookeeperConnectionString(zkPort);

        Properties consumerProperties = new Properties();
        consumerProperties.put("metadata.broker.list", zkConnection);
        consumerProperties.put("zookeeper.connect", zkConnection);
        consumerProperties.put("zookeeper.session.timeout.ms", kafkaMqConfig.getZookeeperSessionTimeoutMilliseconds());
        consumerProperties.put("zookeeper.sync.time.ms", kafkaMqConfig.getZookeeperSynchronisationTimeMilliseconds());
        consumerProperties.put("auto.commit.interval.ms", kafkaMqConfig.getAutoCommitIntervalMilliseconds());
        consumerProperties.put("auto.offset.reset", kafkaMqConfig.getOffsetReset());//seeks to end

        return consumerProperties;
    }

    @Override
    public BaseCoordinationEntryEventProducer<K> getProducer(MQConfiguration config) {
        return new KafkaEventProducer<>(createProducerConfig(), config.getTopicName());
    }

    @Override
    public BaseCoordinationEntryEventConsumer<K> getConsumer(MQConfiguration config) throws IOException, JMSException {
        return new KafkaEventConsumer<>(createConsumerConfig(), config.getTopicName(), this.kafkaMqConfig);
    }

    @Override
    public void close() throws IOException {
    }
}
