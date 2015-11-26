package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;

public class MacheBuilder<K, V> {

    public static <K, V> FluentMacheBuilder<K, V> mache(Class<K> keyType, Class<V> valueType) {
        return cacheProvisioner -> storageProvisioner -> messagingProvisioner ->
                new MacheBuilder<>(keyType, valueType, cacheProvisioner, storageProvisioner, messagingProvisioner);
    }

    public interface FluentMacheBuilder<K, V> {
        StorageProvisionerBuilder<K, V> cachedBy(CacheProvisioner<K, V> storageProvisioner);
    }

    public interface StorageProvisionerBuilder<K, V> {
        MessagingProvisionerBuilder<K, V> storedIn(StorageProvisioner storageProvisioner);
    }

    public interface MessagingProvisionerBuilder<K, V> {
        MacheBuilder<K, V> withMessaging(MessagingProvisioner messagingProvisioner);

        default MacheBuilder<K, V> withNoMessaging() {
            return withMessaging(new NoMessagingProvisioner());
        }
    }

    private final Class<K> keyType;
    private final Class<V> valueType;
    private CacheProvisioner<K, V> cacheProvisioner;
    private final StorageProvisioner storageProvisioner;
    private final MessagingProvisioner messagingProvisioner;

    private MacheBuilder(Class<K> keyType, Class<V> valueType, CacheProvisioner<K, V> cacheProvisioner,
                         StorageProvisioner storageProvisioner, MessagingProvisioner messagingProvisioner) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.cacheProvisioner = cacheProvisioner;
        this.storageProvisioner = storageProvisioner;
        this.messagingProvisioner = messagingProvisioner;
    }

    /**
     * Utilises the provided MacheBuilder instance with Cache, Storage and Messaging providers to
     * build a Mache instance.
     * @return the built and connected Mache object.
     * @throws Exception any exception that occurred during creation.
     */
    public Mache<K, V> macheUp() throws Exception {
        MacheLoader<K, V, ?> cacheLoader = storageProvisioner.getCacheLoader(keyType, valueType);
        cacheLoader.create();
        Mache<K, V> cache = cacheProvisioner.create(keyType, valueType, cacheLoader);
        return messagingProvisioner.wireInMessaging(cache);
    }
}