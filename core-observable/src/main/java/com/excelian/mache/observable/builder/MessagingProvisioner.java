package com.excelian.mache.observable.builder;

import com.excelian.mache.core.Mache;

import java.io.IOException;
import javax.jms.JMSException;

/**
 * Created by jbowkett on 11/08/15.
 */
public interface MessagingProvisioner {
    String getMessaging();

    <K, V> Mache<K, V> wireInMessaging(Mache<K, V> toWireIn, String topic, String messagingLocation) throws IOException, JMSException;
}
