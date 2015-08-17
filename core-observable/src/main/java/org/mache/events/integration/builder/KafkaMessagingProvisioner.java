package org.mache.events.integration.builder;

import org.mache.builder.MessagingProvisioner;
import org.mache.events.MQFactory;
import org.mache.events.integration.KafkaMQFactory;

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
