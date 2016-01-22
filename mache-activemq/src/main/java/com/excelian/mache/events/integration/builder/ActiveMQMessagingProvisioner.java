package com.excelian.mache.events.integration.builder;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.ActiveMQFactory;
import com.excelian.mache.observable.builder.AbstractMessagingProvisioner;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import static java.util.concurrent.TimeUnit.MINUTES;
import static javax.jms.DeliveryMode.NON_PERSISTENT;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

/**
 * Provisions Active MQ Messaging Providers.
 */
public class ActiveMQMessagingProvisioner extends AbstractMessagingProvisioner {

    private final ConnectionFactory connectionFactory;
    private final long timeToLiveInMillis;
    private final int deliveryMode;
    private final int acknowledgementMode;

    private ActiveMQMessagingProvisioner(String topic, ConnectionFactory connectionFactory, long timeToLiveInMillis,
                                         int deliveryMode, int acknowledgementMod) {
        super(topic);
        this.connectionFactory = connectionFactory;
        this.timeToLiveInMillis = timeToLiveInMillis;
        this.deliveryMode = deliveryMode;
        this.acknowledgementMode = acknowledgementMod;
    }

    public static TopicBuilder activemq() {
        return topic -> connectionFactory -> new ActiveMQMessagingProvisionerBuilder(connectionFactory, topic);
    }

    @Override
    public <K> MQFactory<K> getMqFactory() throws JMSException {
        return new ActiveMQFactory<>(connectionFactory, timeToLiveInMillis, deliveryMode, acknowledgementMode);
    }

    /**
     * Adds the topic to use for the Active MQ Messaging Provider.
     */
    public interface TopicBuilder {
        ConnectionFactoryBuilder withTopic(String topic);
    }

    /**
     * Adds the Active MQ Connection Factory to use for the Active MQ Messaging Provider.
     */
    public interface ConnectionFactoryBuilder {
        ActiveMQMessagingProvisionerBuilder withConnectionFactory(ConnectionFactory connectionFactory);
    }

    /**
     * Builder for Active MQ Provisioner.
     */
    public static class ActiveMQMessagingProvisionerBuilder {
        private final ConnectionFactory connectionFactory;
        private final String topic;
        private long timeToLiveInMillis = MINUTES.toMillis(1);
        private int deliveryMode = NON_PERSISTENT;
        private int acknowledgementMode = AUTO_ACKNOWLEDGE;

        private ActiveMQMessagingProvisionerBuilder(ConnectionFactory connectionFactory, String topic) {
            this.connectionFactory = connectionFactory;
            this.topic = topic;
        }

        public ActiveMQMessagingProvisionerBuilder withTimeToLiveInMillis(long timeToLiveInMillis) {
            this.timeToLiveInMillis = timeToLiveInMillis;
            return this;
        }

        public ActiveMQMessagingProvisionerBuilder withDeliveryMode(int deliveryMode) {
            this.deliveryMode = deliveryMode;
            return this;
        }

        public ActiveMQMessagingProvisionerBuilder withAcknowledgementMode(int acknowledgementMode) {
            this.acknowledgementMode = acknowledgementMode;
            return this;
        }

        public ActiveMQMessagingProvisioner build() {
            return new ActiveMQMessagingProvisioner(topic, connectionFactory, timeToLiveInMillis, deliveryMode,
                    acknowledgementMode);
        }

    }

}
