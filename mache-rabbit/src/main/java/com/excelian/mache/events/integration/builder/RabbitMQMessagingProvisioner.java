package com.excelian.mache.events.integration.builder;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.DefaultRabbitMqConfig;
import com.excelian.mache.events.integration.RabbitMQFactory;
import com.excelian.mache.observable.builder.AbstractMessagingProvisioner;

import java.io.IOException;
import javax.jms.JMSException;

/**
 * Created by jbowkett on 11/08/15.
 */
public class RabbitMQMessagingProvisioner extends AbstractMessagingProvisioner {

    @Override
    public String getMessaging() {
        return "RabbitMQ";
    }

    @Override
    public MQFactory getMqFactory(String messagingLocation) throws IOException, JMSException {
        return new RabbitMQFactory(messagingLocation, new DefaultRabbitMqConfig());
    }
}
