package com.excelian.mache.builder.storage;

import com.excelian.mache.core.Cache;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;

public interface StorageProvisioner {
    <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType, Cache<K, V> inMemoryCache);

    <K, V> MacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType);
}
