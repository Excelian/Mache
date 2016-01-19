package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;

/**
 * Provisions caches for Mache.
 *
 * @param <K> key type of the cache to be provisioned.
 * @param <V> value type of the cache to be provisioned.
 *
 * TODO Jamie -> can the generic be taken off the class definition? Makes this hard to pass around
 */
public interface CacheProvisioner<K, V>  {

    Mache<K, V> create(Class<K> keyType, Class<V> valueType, MacheLoader<K, V, ?> cacheLoader);
}
