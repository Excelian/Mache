package com.excelian.mache.builder;

import com.excelian.mache.builder.storage.StorageProvisioner;
import com.excelian.mache.core.Mache;

public class MacheBuilder<K, V> {

    public static <K, V> FluentMacheBuilder<K, V> mache(Class<K> keyType, Class<V> valueType) {
        return storageProvisioners -> messagingProvisioner ->
            new MacheBuilder<>(keyType, valueType, storageProvisioners, messagingProvisioner);
    }

    public interface FluentMacheBuilder<K, V> {
        StorageProvisionerBuilder<K, V> backedBy(StorageProvisioner storageProvisioner);
    }

    public interface StorageProvisionerBuilder<K, V> {
        MacheBuilder<K, V> withMessaging(MessagingProvisioner messagingProvisioner);

        default MacheBuilder<K, V> withNoMessaging() {
            return withMessaging(new NoMessagingProvisioner());
        }
    }

    private final Class<K> keyType;
    private final Class<V> valueType;
    private final StorageProvisioner storageProvisioner;
    private final MessagingProvisioner messagingProvisioner;

    private MacheBuilder(Class<K> keyType, Class<V> valueType, StorageProvisioner storageProvisioner,
                         MessagingProvisioner messagingProvisioner) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.storageProvisioner = storageProvisioner;
        this.messagingProvisioner = messagingProvisioner;
    }

    public Mache<K, V> macheUp() throws Exception {
        final Mache<K, V> cache = storageProvisioner.getCache(keyType, valueType);
        return messagingProvisioner.wireInMessaging(cache);
    }
}