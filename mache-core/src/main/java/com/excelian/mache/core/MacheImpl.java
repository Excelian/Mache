package com.excelian.mache.core;

import com.fasterxml.uuid.Generators;

import java.util.UUID;


public class MacheImpl<K, V> implements Mache<K, V> {

    private final Cache<K,V> inMemoryCache;
    private final MacheLoader<K, V, ?> cacheLoader;
    private final UUID cacheId;


    public MacheImpl(Cache<K, V> inMemoryCache, final MacheLoader<K, V, ?> cacheLoader) {
        this.inMemoryCache = inMemoryCache;
        this.cacheLoader = cacheLoader;
        cacheLoader.create();
        inMemoryCache.create(cacheLoader);

        cacheId = Generators.nameBasedGenerator().generate(getName() + String.valueOf(this));
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
        final V unchecked = inMemoryCache.getUnchecked(key);
        if (unchecked == null){
            final V loaded = loadUnchecked(key);
            inMemoryCache.put(key, loaded);
            return loaded;
        }
        else {
            return unchecked;
        }
    }

    private V loadUnchecked(K key) {
        try {
            return cacheLoader.load(key);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void put(K key, V value) {
        inMemoryCache.invalidate(key);
        cacheLoader.put(key, value);
    }

    @Override
    public void remove(K key) {
        cacheLoader.remove(key);
        inMemoryCache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        inMemoryCache.invalidateAll();
    }

    @Override
    public void invalidate(K key) {
        inMemoryCache.invalidate(key);
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
