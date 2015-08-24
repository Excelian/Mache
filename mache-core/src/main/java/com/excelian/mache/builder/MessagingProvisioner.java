package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;


/**
 * Created by jbowkett on 11/08/15.
 */
public interface MessagingProvisioner {
    String getMessaging();

    <K, V> Mache<K, V> wireInMessaging(Mache<K, V> toWireIn, String topic, String messagingLocation) throws Exception;
}
