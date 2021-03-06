package com.excelian.mache.events.integration.builder;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.RabbitMQFactory;
import com.excelian.mache.events.integration.RabbitMQConfig;
import com.excelian.mache.events.integration.RabbitMQConfig.RabbitMQConfigBuilder;
import com.excelian.mache.observable.builder.AbstractMessagingProvisioner;
import com.rabbitmq.client.ConnectionFactory;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Provisions Rabbit MQ messaging from config.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class RabbitMQMessagingProvisioner<K, V> extends AbstractMessagingProvisioner<K, V> {

    private final ConnectionFactory connectionFactory;
    private final RabbitMQConfig rabbitMqConfig;

    private RabbitMQMessagingProvisioner(String topic,
                                         ConnectionFactory connectionFactory,
                                         RabbitMQConfig rabbitMqConfig) {
        super(topic);
        this.connectionFactory = connectionFactory;
        this.rabbitMqConfig = rabbitMqConfig;
    }

    @Override
    public MQFactory<K> getMqFactory() throws IOException, JMSException {
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
    public static class RabbitMqMessagingProvisionerBuilder {
        private final String topic;
        private final ConnectionFactory connectionFactory;
        private RabbitMQConfig rabbitMqConfig = RabbitMQConfigBuilder.builder().build();

        public RabbitMqMessagingProvisionerBuilder(String topic, ConnectionFactory connectionFactory) {
            this.topic = topic;
            this.connectionFactory = connectionFactory;
        }

        public RabbitMqMessagingProvisionerBuilder withRabbitMqConfig(RabbitMQConfig rabbitMqConfig) {
            this.rabbitMqConfig = rabbitMqConfig;
            return this;
        }

        public <K, V> RabbitMQMessagingProvisioner<K, V> build() {
            connectionFactory.setNetworkRecoveryInterval(rabbitMqConfig.getNetworkRecoveryInterval());
            return new RabbitMQMessagingProvisioner<>(topic, connectionFactory, rabbitMqConfig);
        }
    }
}
