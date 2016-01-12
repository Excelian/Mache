package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;

/**
 * A no op Messaging Provisioner.
 */
public class NoMessagingProvisioner implements MessagingProvisioner {
    @Override
    public <K, V> Mache<K, V> wireInMessaging(Mache<K, V> toWireIn) throws Exception {
        return toWireIn;
    }
}
