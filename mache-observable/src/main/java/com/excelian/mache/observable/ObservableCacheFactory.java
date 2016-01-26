package com.excelian.mache.observable;

import com.excelian.mache.core.Mache;

/**
 * Creates observable caches out of a given mache.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ObservableCacheFactory<K, V> {
    Mache<K, V> createCache(Mache<K, V> underlyingCache);
}
