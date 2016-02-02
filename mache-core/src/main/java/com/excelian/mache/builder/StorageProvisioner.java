package com.excelian.mache.builder;

import com.excelian.mache.core.MacheLoader;

/**
 * Provisions backing storage for Mache.
 *
 * @param <K> the key type.
 * @param <V> the value type.
 */
public interface StorageProvisioner<K, V> {
    MacheLoader<K, V> getCacheLoader(Class<K> keyType, Class<V> valueType);
}
