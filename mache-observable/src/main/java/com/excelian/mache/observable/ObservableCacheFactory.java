package com.excelian.mache.observable;

import com.excelian.mache.core.Mache;

public interface ObservableCacheFactory<K, V> {
    Mache<K, V> createCache(Mache<K, V> underlyingCache);
}
