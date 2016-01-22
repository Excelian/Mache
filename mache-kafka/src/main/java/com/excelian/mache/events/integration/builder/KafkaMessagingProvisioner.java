package com.excelian.mache.events.integration.builder;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.KafkaMqConfig;
import com.excelian.mache.events.integration.KafkaMQFactory;
import com.excelian.mache.observable.builder.AbstractMessagingProvisioner;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Provisions kafka messaging.
 */
public class KafkaMessagingProvisioner extends AbstractMessagingProvisioner {

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
        return kafkaMqConfig -> topic -> new KafkaMessagingProvisioner(topic, kafkaMqConfig);
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
        KafkaMessagingProvisioner withTopic(String topic);
    }

    @Override
    public <K> MQFactory<K> getMqFactory() throws IOException, JMSException {
        return new KafkaMQFactory<>(kafkaMqConfig);
    }
}
