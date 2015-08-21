package com.excelian.mache.observable.builder;

import com.excelian.mache.core.Mache;

import java.io.IOException;
import javax.jms.JMSException;

/**
 * Created by jbowkett on 21/08/2015.
 */
public class NoMessagingProvisioner implements MessagingProvisioner {
    @Override
    public String getMessaging() {
        return "None";
    }

    @Override
    public <K, V> Mache<K, V> wireInMessaging(Mache<K, V> toWireIn, String topic, String messagingLocation) throws IOException, JMSException {
        return toWireIn;
    }
}
