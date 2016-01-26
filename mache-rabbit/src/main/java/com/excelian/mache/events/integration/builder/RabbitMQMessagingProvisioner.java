package com.excelian.mache.events.integration.builder;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.RabbitMQFactory;
import com.excelian.mache.events.integration.RabbitMqConfig;
import com.excelian.mache.events.integration.RabbitMqConfig.RabbitMqConfigBuilder;
import com.excelian.mache.observable.builder.AbstractMessagingProvisioner;
import com.rabbitmq.client.ConnectionFactory;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Provisions Rabbit MQ messaging from config.
 */
public class RabbitMQMessagingProvisioner extends AbstractMessagingProvisioner {

    private final ConnectionFactory connectionFactory;
    private final RabbitMqConfig rabbitMqConfig;

    private RabbitMQMessagingProvisioner(String topic,
                                         ConnectionFactory connectionFactory,
                                         RabbitMqConfig rabbitMqConfig) {
        super(topic);
        this.connectionFactory = connectionFactory;
        this.rabbitMqConfig = rabbitMqConfig;
    }

    @Override
    public <K> MQFactory<K> getMqFactory() throws IOException, JMSException {
        return new RabbitMQFactory<>(connectionFactory, rabbitMqConfig);
    }

    public static TopicBuilder rabbitMq() {
        return topic -> connectionFactory -> new RabbitMqMessagingProvisionerBuilder(topic, connectionFactory);
    }

    /**
     * Enforces topic to be specified.
     */
    public interface TopicBuilder {
        ConnectionFactoryBuilder withTopic(String topic);
    }

    /**
     * Enforces connection details to be specified.
     */
    public interface ConnectionFactoryBuilder {
        RabbitMqMessagingProvisionerBuilder withConnectionFactory(ConnectionFactory connectionFactory);
    }

    /**
     * Builder for Rabbit MQ.
     */
    private static class RabbitMqMessagingProvisionerBuilder {
        private final String topic;
        private final ConnectionFactory connectionFactory;
        private RabbitMqConfig rabbitMqConfig = RabbitMqConfigBuilder.builder().build();

        public RabbitMqMessagingProvisionerBuilder(String topic, ConnectionFactory connectionFactory) {
            this.topic = topic;
            this.connectionFactory = connectionFactory;
        }

        public RabbitMqMessagingProvisionerBuilder withRabbitMqConfig(RabbitMqConfig rabbitMqConfig) {
            this.rabbitMqConfig = rabbitMqConfig;
            return this;
        }

        public RabbitMQMessagingProvisioner build() {
            connectionFactory.setNetworkRecoveryInterval(rabbitMqConfig.getNetworkRecoveryInterval());
            return new RabbitMQMessagingProvisioner(topic, connectionFactory, rabbitMqConfig);
        }
    }
}
