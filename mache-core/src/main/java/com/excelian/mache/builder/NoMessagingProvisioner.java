package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;

/**
 * A no op Messaging Provisioner.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public class NoMessagingProvisioner<K, V> implements MessagingProvisioner<K, V> {
    @Override
    public Mache<K, V> wireInMessaging(Mache<K, V> toWireIn) throws Exception {
        return toWireIn;
    }
}
