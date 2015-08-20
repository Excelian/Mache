package com.excelian.mache.events.integration.builder;

import com.excelian.mache.builder.MessagingProvisioner;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.ActiveMQFactory;

import javax.jms.JMSException;

/**
 * Created by jbowkett on 11/08/15.
 */
public class ActiveMQMessagingProvisioner implements MessagingProvisioner {
  @Override
  public String getMessaging() {
    return "ActiveMQ";
  }

  @Override
  public MQFactory getMQFactory(String messagingLocation) throws JMSException {
    return new ActiveMQFactory(messagingLocation);
  }
}
