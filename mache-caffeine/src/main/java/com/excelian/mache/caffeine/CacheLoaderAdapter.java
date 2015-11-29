package com.excelian.mache.caffeine;

import com.excelian.mache.core.MacheLoader;
import com.github.benmanes.caffeine.cache.CacheLoader;

public class CacheLoaderAdapter<K, V> implements CacheLoader<K, V> {

    private MacheLoader<K, V, ?> cacheLoader;

    public CacheLoaderAdapter(MacheLoader<K, V, ?> cacheLoader) {
        this.cacheLoader = cacheLoader;
    }

    @Override
    public V load(K key) throws Exception {
        return cacheLoader.load(key);
    }

}
