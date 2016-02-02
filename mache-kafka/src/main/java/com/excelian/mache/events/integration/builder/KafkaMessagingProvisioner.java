package com.excelian.mache.events.integration.builder;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.KafkaMqConfig;
import com.excelian.mache.events.integration.KafkaMQFactory;
import com.excelian.mache.observable.builder.AbstractMessagingProvisioner;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Provisions kafka messaging.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class KafkaMessagingProvisioner<K, V> extends AbstractMessagingProvisioner<K, V> {

    private final KafkaMqConfig kafkaMqConfig;

    /**
     * Constructor.
     * @param topic - the topic to use
     * @param kafkaMqConfig - kafka config
     */
    private KafkaMessagingProvisioner(String topic, KafkaMqConfig kafkaMqConfig) {
        super(topic);
        this.kafkaMqConfig = kafkaMqConfig;
    }

    public static KafkaMqConfigBuilder kafka() {
        return kafkaMqConfig -> topic -> new KafkaMessagingProvisionerBuilder(topic, kafkaMqConfig);
    }

    /**
     * Enforces specification kafka config.
     */
    public interface KafkaMqConfigBuilder {
        TopicBuilder withKafkaMqConfig(KafkaMqConfig kafkaMqConfig);
    }

    /**
     * Enforces specification topic.
     */
    public interface TopicBuilder {
        KafkaMessagingProvisionerBuilder withTopic(String topic);
    }

    /**
     * Builder containing generic build method for a KafkaMessagingProvisioner.
     */
    public static class KafkaMessagingProvisionerBuilder {
        private final String topic;
        private final KafkaMqConfig kafkaMqConfig;

        public KafkaMessagingProvisionerBuilder(String topic, KafkaMqConfig kafkaMqConfig) {
            this.topic = topic;
            this.kafkaMqConfig = kafkaMqConfig;
        }

        public <K, V> KafkaMessagingProvisioner<K, V> build() {
            return new KafkaMessagingProvisioner<>(topic, kafkaMqConfig);
        }
    }

    @Override
    public MQFactory<K> getMqFactory() throws IOException, JMSException {
        return new KafkaMQFactory<>(kafkaMqConfig);
    }
}
