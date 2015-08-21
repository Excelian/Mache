package com.excelian.mache.events.integration;

import com.rabbitmq.client.AMQP.BasicProperties;

/**
 * Created by jbowkett on 21/08/2015.
 */
public interface RabbitMqConfig {
    long getMessageTTLMilliSeconds();

    long getMessageExpiryMilliSeconds();

    int getMaxLength();

    String getExchangeName();

    BasicProperties getRoutingHeader();

    int getNetworkRecoveryIntervalMilliSeconds();
}
