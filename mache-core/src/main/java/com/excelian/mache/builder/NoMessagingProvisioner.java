package com.excelian.mache.builder;

import com.excelian.mache.builder.MessagingProvisioner;
import com.excelian.mache.core.Mache;

/**
 * Created by jbowkett on 21/08/2015.
 */
public class NoMessagingProvisioner implements MessagingProvisioner {
    @Override
    public String getMessaging() {
        return "None";
    }

    @Override
    public <K, V> Mache<K, V> wireInMessaging(Mache<K, V> toWireIn, String topic, String messagingLocation) throws Exception {
        return toWireIn;
    }
}
