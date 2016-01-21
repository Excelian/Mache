package com.excelian.mache.guava;

import com.excelian.mache.core.MacheLoader;
import com.google.common.cache.CacheLoader;

/**
 * Adapts the Guava CacheLoader to a MacheLoader.
 *
 * @param <K> the type of key to store in Mache.
 * @param <V> the type of value to store in Mache.
 */
public class CacheLoaderAdapter<K, V> extends CacheLoader<K, V> {

    final MacheLoader<K, V> macheLoader;

    public CacheLoaderAdapter(MacheLoader<K, V> macheLoader) {
        this.macheLoader = macheLoader;
    }

    @Override
    public V load(K key) throws Exception {
        return macheLoader.load(key);
    }
}
