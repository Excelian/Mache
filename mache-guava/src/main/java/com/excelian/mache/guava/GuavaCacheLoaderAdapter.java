package com.excelian.mache.guava;

import com.excelian.mache.core.MacheLoader;
import com.google.common.cache.CacheLoader;

/**
 * Created by jbowkett on 12/01/2016.
 */
public class GuavaCacheLoaderAdapter<K, V> extends CacheLoader<K, V> {
    private final MacheLoader<K, V, ?> macheCacheLoader;

    public GuavaCacheLoaderAdapter(MacheLoader<K, V, ?> macheCacheLoader) {
        this.macheCacheLoader = macheCacheLoader;
    }

    /**
     * Computes or retrieves the value corresponding to {@code key}.
     *
     * @param key the non-null key whose value should be loaded
     * @return the value associated with {@code key}; <b>must not be null</b>
     * @throws Exception            if unable to load the result
     * @throws InterruptedException if this method is interrupted. {@code InterruptedException} is
     *                              treated like any other {@code Exception} in all respects except that, when it is caught,
     *                              the thread's interrupt status is set
     */
    @Override
    public V load(K key) throws Exception {
        return macheCacheLoader.load(key);
    }
}
