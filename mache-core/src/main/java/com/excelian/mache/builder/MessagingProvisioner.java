package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;


public interface MessagingProvisioner {

    <K, V> Mache<K, V> wireInMessaging(Mache<K, V> toWireIn) throws Exception;

}
