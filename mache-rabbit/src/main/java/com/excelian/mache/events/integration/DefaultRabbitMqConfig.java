package com.excelian.mache.events.integration;

import static com.rabbitmq.client.AMQP.BasicProperties;
import static com.rabbitmq.client.MessageProperties.PERSISTENT_TEXT_PLAIN;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by jbowkett on 21/08/2015.
 */
public class DefaultRabbitMqConfig implements RabbitMqConfig {

    private static final String EXCHANGE_NAME = "coherence-killer-exchange";
    private static final int MAX_LENGTH = 10000;
    private static final int MESSAGE_EXPIRY_SECONDS = 1;
    private static final int NETWORK_RECOVERY_INTERVAL_SECONDS = 1;
    private static final int MESSAGE_TTL_SECONDS = 1;

    public int getMaxLength() {
        return MAX_LENGTH;
    }

    @Override
    public String getExchangeName() {
        return EXCHANGE_NAME;
    }

    public long getMessageTTLMilliSeconds() {
        return MINUTES.toMillis(MESSAGE_TTL_SECONDS);
    }

    public long getMessageExpiryMilliSeconds() {
        return MINUTES.toMillis(MESSAGE_EXPIRY_SECONDS);
    }

    public BasicProperties getRoutingHeader() {
        return PERSISTENT_TEXT_PLAIN;
    }

    public int getNetworkRecoveryIntervalMilliSeconds() {
        return (int) SECONDS.toMillis(NETWORK_RECOVERY_INTERVAL_SECONDS);
    }
}
