package com.excelian.mache.events.integration.builder;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.ActiveMQFactory;
import com.excelian.mache.events.integration.DefaultActiveMqConfig;
import com.excelian.mache.observable.builder.AbstractMessagingProvisioner;

import javax.jms.JMSException;

/**
 * Created by jbowkett on 11/08/15.
 */
public class ActiveMQMessagingProvisioner extends AbstractMessagingProvisioner {
    @Override
    public String getMessaging() {
        return "ActiveMQ";
    }

    @Override
    public MQFactory getMqFactory(String messagingLocation) throws JMSException {
        return new ActiveMQFactory(messagingLocation, new DefaultActiveMqConfig());
    }
}
