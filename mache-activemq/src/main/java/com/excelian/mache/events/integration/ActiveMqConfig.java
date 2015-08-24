package com.excelian.mache.events.integration;

/**
 * Created by jbowkett on 21/08/2015.
 */
public interface ActiveMqConfig {
    long getTimeToLiveInMillis();

    int getDeliveryMode();

    int getAutoAcknowledge();
}
