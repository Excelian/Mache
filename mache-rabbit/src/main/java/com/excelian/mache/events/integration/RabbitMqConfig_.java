package com.excelian.mache.events.integration;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Type for the Rabbit MQ config.
 */
public class RabbitMqConfig_ {

    private final String exchangeName;
    private final int maxLength;
    private final int messageExpiry;
    private final int networkRecoveryInterval;
    private final int messageTtl;
    private final BasicProperties routingHeader;

    /**
     * Constructor.
     *
     * @param exchangeName            - exchangeName
     * @param maxLength               - maxLength
     * @param messageExpiry           - messageExpiry
     * @param networkRecoveryInterval - networkRecoveryInterval
     * @param messageTtl              - messageTtl
     * @param routingHeader           - routingHeader
     */
    private RabbitMqConfig_(String exchangeName, int maxLength,
                            int messageExpiry, int networkRecoveryInterval,
                            int messageTtl, BasicProperties routingHeader) {
        this.exchangeName = exchangeName;
        this.maxLength = maxLength;
        this.messageExpiry = messageExpiry;
        this.networkRecoveryInterval = networkRecoveryInterval;
        this.messageTtl = messageTtl;
        this.routingHeader = routingHeader;
    }

    /**
     * Builder to produce Rabbit MQ config.
     */
    public static class RabbitMQConfigBuilder {
        private String exchangeName = "default-exchange";
        private int maxLength = 10000;
        private int messageExpiry = (int) SECONDS.toMillis(1);
        private int networkRecoveryInterval = (int) SECONDS.toMillis(1);
        private int messageTtl = (int) SECONDS.toMillis(1);
        private BasicProperties routingHeader = MessageProperties.PERSISTENT_TEXT_PLAIN;


        public static RabbitMQConfigBuilder builder() {
            return new RabbitMQConfigBuilder();
        }

        public RabbitMQConfigBuilder withExchangeName(String exchangeName) {
            this.exchangeName = exchangeName;
            return this;
        }

        public RabbitMQConfigBuilder withMaxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public RabbitMQConfigBuilder withMessageExpiry(int messageExpiry) {
            this.messageExpiry = messageExpiry;
            return this;
        }

        public RabbitMQConfigBuilder withNetworkRecoveryInterval(int networkRecoveryInterval) {
            this.networkRecoveryInterval = networkRecoveryInterval;
            return this;
        }

        public RabbitMQConfigBuilder withMessageTtl(int messageTtl) {
            this.messageTtl = messageTtl;
            return this;
        }

        public RabbitMQConfigBuilder withRoutingHeader(BasicProperties routingHeader) {
            this.routingHeader = routingHeader;
            return this;
        }

        public RabbitMqConfig_ build() {
            return new RabbitMqConfig_(exchangeName, maxLength, messageExpiry,
                networkRecoveryInterval, messageTtl, routingHeader);
        }
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getMessageExpiry() {
        return messageExpiry;
    }

    public int getNetworkRecoveryInterval() {
        return networkRecoveryInterval;
    }

    public int getMessageTtl() {
        return messageTtl;
    }

    public BasicProperties getRoutingHeader() {
        return routingHeader;
    }
}
