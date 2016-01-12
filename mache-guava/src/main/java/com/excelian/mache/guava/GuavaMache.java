package com.excelian.mache.guava;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.fasterxml.uuid.Generators;
import com.google.common.cache.LoadingCache;

import java.util.UUID;

/**
 * A Mache with a Guava In Memory store.
 *
 * @param <K> the type of key to store in Mache.
 * @param <V> the type of value to store in Mache.
 */
public class GuavaMache<K, V> implements Mache<K, V> {

    private final MacheLoader<K, V, ?> cacheLoader;
    private final UUID cacheId;

    private final LoadingCache<K, V> cache;

    /**
     * @param cacheLoader The MacheLoader of the backing store for this Mache.
     * @param cache The created Guava LoadingCache.
     */
    public GuavaMache(final MacheLoader<K, V, ?> cacheLoader, LoadingCache<K, V> cache) {
        this.cacheLoader = cacheLoader;
        this.cache = cache;
        cacheId = Generators.nameBasedGenerator().generate(getName() + toString());
    }

    @Override
    public String getName() {
        return cacheLoader.getName();
    }

    @Override
    public UUID getId() {
        return cacheId;
    }

    @Override
    public V get(final K key) {
        return cache.getUnchecked(key);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        cacheLoader.put(key, value);
    }

    @Override
    public void remove(K key) {
        cacheLoader.remove(key);
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public void invalidate(K key) {
        cache.invalidate(key);
    }

    @Override
    public void close() {
        cacheLoader.close();
    }

    @Override
    public MacheLoader getCacheLoader() {
        return cacheLoader;
    }
}
