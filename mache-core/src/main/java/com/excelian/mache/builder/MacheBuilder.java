package com.excelian.mache.builder;

import com.excelian.mache.builder.storage.StorageProvisioner;
import com.excelian.mache.core.Mache;

public class MacheBuilder<K, V> {

    public static <K, V> FluentMacheBuilder<K, V> mache(Class<K> keyType, Class<V> valueType) {
        return cacheProvisioner -> storageProvisioner -> messagingProvisioner ->
                new MacheBuilder<>(keyType, valueType, storageProvisioner, messagingProvisioner, cacheProvisioner);
    }

    public interface FluentMacheBuilder<K, V> {
        CacheProvisionerBuilder<K, V> cachedBy(CacheProvisioner storageProvisioner);
    }

    public interface CacheProvisionerBuilder<K, V> {
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
    private final CacheProvisioner cacheProvisioner;

    private MacheBuilder(Class<K> keyType, Class<V> valueType, StorageProvisioner storageProvisioner,
                         MessagingProvisioner messagingProvisioner, CacheProvisioner cacheProvisioner) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.storageProvisioner = storageProvisioner;
        this.messagingProvisioner = messagingProvisioner;
        this.cacheProvisioner = cacheProvisioner;
    }

    public Mache<K, V> macheUp() throws Exception {
        Mache<K, V> cache = storageProvisioner.getCache(keyType, valueType, cacheProvisioner.getCache());
        return messagingProvisioner.wireInMessaging(cache);
    }
}