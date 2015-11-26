package com.excelian.mache.guava;

import com.excelian.mache.core.MacheLoader;
import com.google.common.cache.CacheLoader;

public class CacheLoaderAdapter<K, V, D> extends CacheLoader<K, V> {

    final MacheLoader<K, V, D> macheLoader;

    public CacheLoaderAdapter(MacheLoader<K, V, D> macheLoader) {
        this.macheLoader = macheLoader;
    }

    @Override
    public V load(K key) throws Exception {
        return macheLoader.load(key);
    }
}
