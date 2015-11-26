package com.excelian.mache.builder;

import com.excelian.mache.core.MacheLoader;

public interface StorageProvisioner {
    <K, V> MacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType);
}
