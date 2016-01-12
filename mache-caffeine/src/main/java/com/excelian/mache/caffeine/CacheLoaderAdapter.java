package com.excelian.mache.caffeine;

import com.excelian.mache.core.MacheLoader;
import com.github.benmanes.caffeine.cache.CacheLoader;

/**
 * Adapts the Caffeine CacheLoader to a MacheLoader.
 *
 * @param <K> the type of key to store in Mache.
 * @param <V> the type of value to store in Mache.
 */
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
