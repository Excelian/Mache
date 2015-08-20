package com.excelian.mache.observable;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;

// This is something like an "ObservableCacheFactory"
public interface CacheFactory {
	<K, V, D> Mache<K, V> createCache(MacheLoader<K, V, D> cacheLoader);
	<K, V> Mache<K, V> createCache(Mache<K, V> underlyingCache);
}
