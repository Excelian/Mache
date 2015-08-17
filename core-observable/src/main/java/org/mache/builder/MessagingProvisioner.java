package org.mache.builder;

import org.mache.events.MQFactory;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Created by jbowkett on 11/08/15.
 */
public interface MessagingProvisioner {
  String getMessaging();
  MQFactory getMQFactory(String messagingLocation) throws IOException, JMSException;
}
