package com.excelian.mache.events.integration.builder;

import com.excelian.mache.observable.builder.MessagingProvisioner;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.KafkaMQFactory;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Created by jbowkett on 11/08/15.
 */
public class KafkaMessagingProvisioner implements MessagingProvisioner {
  @Override
  public String getMessaging() {
    return "Kafka";
  }

  @Override
  public MQFactory getMQFactory(String messagingLocation) throws IOException, JMSException {
    return new KafkaMQFactory(messagingLocation);

  }
}
