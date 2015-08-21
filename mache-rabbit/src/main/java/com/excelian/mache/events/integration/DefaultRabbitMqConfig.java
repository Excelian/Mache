package com.excelian.mache.events.integration;

import static com.rabbitmq.client.AMQP.*;
import static com.rabbitmq.client.MessageProperties.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by jbowkett on 21/08/2015.
 */
public class DefaultRabbitMqConfig implements RabbitMqConfig {

    public int getMaxLength() {
        return 10000;
    }

    @Override
    public String getExchangeName() {
        return "coherence-killer-exchange";
    }

    public long getMessageTTLMilliSeconds() {
        return TimeUnit.MINUTES.toMillis(1);
    }

    public long getMessageExpiryMilliSeconds() {
        return TimeUnit.MINUTES.toMillis(1);
    }

    public BasicProperties getRoutingHeader() {
        return PERSISTENT_TEXT_PLAIN;
    }

    public int getNetworkRecoveryIntervalMilliSeconds() {
        return 1000;
    }
}
