package com.excelian.mache.events.integration.builder;

import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.KafkaMQFactory;
import com.excelian.mache.observable.builder.AbstractMessagingProvisioner;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Created by jbowkett on 11/08/15.
 */
public class KafkaMessagingProvisioner extends AbstractMessagingProvisioner {
  @Override
  public String getMessaging() {
    return "Kafka";
  }

  @Override
  public MQFactory getMqFactory(String messagingLocation) throws IOException, JMSException {
    return new KafkaMQFactory(messagingLocation);
  }
}
