package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;

/**
 * Builds a Mache instance
 *
 * @param <K> key type of the cache to be provisioned.
 * @param <V> value type of the cache to be provisioned.
 */
public class MacheBuilder<K, V> {

    private final Class<K> keyType;
    private final Class<V> valueType;
    private final StorageProvisioner storageProvisioner;
    private final MessagingProvisioner messagingProvisioner;
    private CacheProvisioner<K, V> cacheProvisioner;

    private MacheBuilder(Class<K> keyType, Class<V> valueType, CacheProvisioner<K, V> cacheProvisioner,
                         StorageProvisioner storageProvisioner, MessagingProvisioner messagingProvisioner) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.cacheProvisioner = cacheProvisioner;
        this.storageProvisioner = storageProvisioner;
        this.messagingProvisioner = messagingProvisioner;
    }

    public static <K, V> FluentMacheBuilder<K, V> mache(Class<K> keyType, Class<V> valueType) {
        return cacheProvisioner -> storageProvisioner -> messagingProvisioner ->
            new MacheBuilder<>(keyType, valueType, cacheProvisioner, storageProvisioner, messagingProvisioner);
    }

    /**
     * Utilises the provided MacheBuilder instance with Cache, Storage and Messaging providers to
     * build a Mache instance.
     *
     * @return the built and connected Mache object.
     * @throws Exception any exception that occurred during creation.
     */
    public Mache<K, V> macheUp() throws Exception {
        MacheLoader<K, V> cacheLoader = storageProvisioner.getCacheLoader(keyType, valueType);
        cacheLoader.create();
        Mache<K, V> cache = cacheProvisioner.create(keyType, valueType, cacheLoader);
        return messagingProvisioner.wireInMessaging(cache);
    }

    /**
     * Adds a cache provisioner.
     *
     * @param <K> key type of the cache to be provisioned.
     * @param <V> value type of the cache to be provisioned.
     */
    public interface FluentMacheBuilder<K, V> {
        StorageProvisionerBuilder<K, V> cachedBy(CacheProvisioner<K, V> cacheProvisioner);
    }

    /**
     * Adds a storage provisioner.
     *
     * @param <K> key type of the cache to be provisioned.
     * @param <V> value type of the cache to be provisioned.
     */
    public interface StorageProvisionerBuilder<K, V> {
        MessagingProvisionerBuilder<K, V> storedIn(StorageProvisioner storageProvisioner);
    }

    /**
     * Adds a messaging provisioner.
     *
     * @param <K> key type of the cache to be provisioned.
     * @param <V> value type of the cache to be provisioned.
     */
    public interface MessagingProvisionerBuilder<K, V> {
        MacheBuilder<K, V> withMessaging(MessagingProvisioner messagingProvisioner);

        default MacheBuilder<K, V> withNoMessaging() {
            return withMessaging(new NoMessagingProvisioner());
        }
    }
}