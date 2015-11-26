package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;

public interface CacheProvisioner<K, V>  {

    Mache<K, V> create(Class<K> keyType, Class<V> valueType, MacheLoader<K, V, ?> cacheLoader);
}
