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

    public static <K, V> KafkaMqConfigBuilder<K, V> kafka(Class<K> keyClass, Class<V> valueClass) {
        return kafkaMqConfig -> topic -> new KafkaMessagingProvisioner<>(topic, kafkaMqConfig);
    }

    /**
     * Enforces specification kafka config.
     * @param <K> key type.
     * @param <V> value type.
     */
    public interface KafkaMqConfigBuilder<K, V> {
        TopicBuilder<K, V> withKafkaMqConfig(KafkaMqConfig kafkaMqConfig);
    }

    /**
     * Enforces specification topic.
     * @param <K> key type.
     * @param <V> value type.
     */
    public interface TopicBuilder<K, V> {
        KafkaMessagingProvisioner<K, V> withTopic(String topic);
    }

    @Override
    public MQFactory<K> getMqFactory() throws IOException, JMSException {
        return new KafkaMQFactory<>(kafkaMqConfig);
    }
}
