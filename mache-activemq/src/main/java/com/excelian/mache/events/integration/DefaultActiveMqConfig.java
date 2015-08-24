package com.excelian.mache.events.integration;

import static java.util.concurrent.TimeUnit.MINUTES;
import static javax.jms.DeliveryMode.NON_PERSISTENT;
import static javax.jms.Session.AUTO_ACKNOWLEDGE;

/**
 * Created by jbowkett on 21/08/2015.
 */
public class DefaultActiveMqConfig implements ActiveMqConfig {

    public long getTimeToLiveInMillis() {
        return MINUTES.toMillis(1);
    }

    public int getDeliveryMode() {
        return NON_PERSISTENT;
    }

    public int getAutoAcknowledge() {
        return AUTO_ACKNOWLEDGE;
    }
}
