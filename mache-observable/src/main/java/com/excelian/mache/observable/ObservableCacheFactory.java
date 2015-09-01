package com.excelian.mache.observable;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;

public interface ObservableCacheFactory<K, V, D> {
    Mache<K, V> createCache(MacheLoader<K, V, D> cacheLoader);

    Mache<K, V> createCache(Mache<K, V> underlyingCache);
}
