package com.excelian.mache.events.integration;

import java.util.concurrent.TimeUnit;
import javax.jms.DeliveryMode;
import javax.jms.Session;

/**
 * Created by jbowkett on 21/08/2015.
 */
public class DefaultActiveMqConfig implements ActiveMqConfig {

    public long getTimeToLiveInMillis() {
        return TimeUnit.MINUTES.toMillis(1);
    }

    public int getDeliveryMode() {
        return DeliveryMode.NON_PERSISTENT;
    }

    public int getAutoAcknowledge() {
        return Session.AUTO_ACKNOWLEDGE;
    }
}
