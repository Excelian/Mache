package com.excelian.mache.observable.builder;

import com.excelian.mache.events.MQFactory;

import java.io.IOException;
import javax.jms.JMSException;

/**
 * Created by jbowkett on 11/08/15.
 */
public interface MessagingProvisioner {
    String getMessaging();

    MQFactory getMQFactory(String messagingLocation) throws IOException, JMSException;
}
