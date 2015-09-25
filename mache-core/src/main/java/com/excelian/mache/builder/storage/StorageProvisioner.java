package com.excelian.mache.builder.storage;

import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.Mache;

public interface StorageProvisioner {
    <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType);

    <K, V> AbstractCacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType);
}
