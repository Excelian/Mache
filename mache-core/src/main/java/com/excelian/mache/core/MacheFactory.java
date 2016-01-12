package com.excelian.mache.core;

public class MacheFactory {

    public <K, V, D> Mache<K, V> create(Cache<K,V> inMemoryCache, MacheLoader<K, V, D> cacheLoader) {
        return new MacheImpl<>(inMemoryCache, cacheLoader);
    }
}
