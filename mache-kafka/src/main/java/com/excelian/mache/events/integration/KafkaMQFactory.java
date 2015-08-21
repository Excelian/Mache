package com.excelian.mache.events.integration;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;

import java.io.IOException;
import java.util.Properties;
import javax.jms.JMSException;

public class KafkaMQFactory implements MQFactory {
    private final String connectionString;
    private final KafkaMqConfig config;

    public KafkaMQFactory(String connectionString, KafkaMqConfig kafkaMqConfig) throws JMSException, IOException {
        this.connectionString = connectionString;
        this.config = kafkaMqConfig;
    }

    Properties createProducerConfig(String zooKeeper) {
        String zkPort = config.getZookeeperProducerPort();
        String zkConnection = config.getZookeeperConnectionString(zooKeeper, zkPort);

        Properties producerProperies = new Properties();
        producerProperies.put("metadata.broker.list", zkConnection);
        producerProperies.put("serializer.class", config.getSerializerClassName());
        producerProperies.put("request.required.acks", config.getRequiredAcks());

        return producerProperies;
    }

    Properties createConsumerConfig(String zooKeeper) {
        String zkPort = config.getZookeeperConsumerPort();
        String zkConnection = config.getZookeeperConnectionString(zooKeeper, zkPort);

        Properties consumerProperties = new Properties();
        consumerProperties.put("metadata.broker.list", zkConnection);
        consumerProperties.put("zookeeper.connect", zkConnection);
        consumerProperties.put("zookeeper.session.timeout.ms", config.getZookeeperSessionTimeoutMilliseconds());
        consumerProperties.put("zookeeper.sync.time.ms", config.getZookeeperSynchronisationTimeMilliseconds());
        consumerProperties.put("auto.commit.interval.ms", config.getAutoCommitIntervalMilliseconds());
        consumerProperties.put("auto.offset.reset", config.getOffsetReset());//seeks to end

        return consumerProperties;
    }

    @Override
    public BaseCoordinationEntryEventProducer getProducer(MQConfiguration config) {
        return new KafkaEventProducer(createProducerConfig(connectionString), config.getTopicName());
    }

    @Override
    public BaseCoordinationEntryEventConsumer getConsumer(MQConfiguration config) throws IOException, JMSException {
        return new KafkaEventConsumer(createConsumerConfig(connectionString), config.getTopicName(), this.config);
    }

    @Override
    public void close() throws IOException {
    }
}
