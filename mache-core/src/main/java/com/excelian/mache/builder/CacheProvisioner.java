package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;

/**
 * Provisions caches for Mache.
 *
 * @param <K> key type of the cache to be provisioned.
 * @param <V> value type of the cache to be provisioned.
 */
public interface CacheProvisioner<K, V> {
    /**
     * Creates a new Mache instance with a MacheLoader
     *
     * @param keyType     key type of the cache to be provisioned.
     * @param valueType   value type of the cache to be provisioned.
     * @param cacheLoader the cache loader to retrieve elements
     * @return Mache instance for the specified key value types
     */
    Mache<K, V> create(Class<K> keyType, Class<V> valueType, MacheLoader<K, V> cacheLoader);
}
