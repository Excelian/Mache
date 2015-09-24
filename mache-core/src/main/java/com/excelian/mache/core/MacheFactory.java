package com.excelian.mache.core;

public class MacheFactory {

    public <K, V, D> Mache<K, V> create(AbstractCacheLoader<K, V, D> cacheLoader, String... options) {
        return new MacheImpl<>(cacheLoader, options);
    }
}
