package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;

/**
 * Provisions a Messaging Provider.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public interface MessagingProvisioner<K, V> {

    Mache<K, V> wireInMessaging(Mache<K, V> toWireIn) throws Exception;

}
