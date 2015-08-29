package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;


public interface MessagingProvisioner {

    // TODO , String topic, String messagingLocation
    <K, V> Mache<K, V> wireInMessaging(Mache<K, V> toWireIn) throws Exception;

}
