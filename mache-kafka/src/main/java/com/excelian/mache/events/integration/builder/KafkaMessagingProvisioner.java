package com.excelian.mache.events.integration.builder;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.KafkaMqConfig;
import com.excelian.mache.events.integration.KafkaMQFactory;
import com.excelian.mache.observable.builder.AbstractMessagingProvisioner;

import javax.jms.JMSException;
import java.io.IOException;

public class KafkaMessagingProvisioner extends AbstractMessagingProvisioner {

    private final KafkaMqConfig kafkaMqConfig;

    private KafkaMessagingProvisioner(String topic, KafkaMqConfig kafkaMqConfig) {
        super(topic);
        this.kafkaMqConfig = kafkaMqConfig;
    }

    public static KafkaMqConfigBuilder kafka() {
        return kafkaMqConfig -> topic -> new KafkaMessagingProvisioner(topic, kafkaMqConfig);
    }

    public interface KafkaMqConfigBuilder {
        TopicBuilder withKafkaMqConfig(KafkaMqConfig kafkaMqConfig);
    }

    public interface TopicBuilder {
        KafkaMessagingProvisioner withTopic(String topic);
    }

    @Override
    public <K> MQFactory<K> getMqFactory() throws IOException, JMSException {
        return new KafkaMQFactory<>(kafkaMqConfig);
    }
}
