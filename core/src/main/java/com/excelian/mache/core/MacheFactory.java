package com.excelian.mache.core;

public class MacheFactory {
    public <K, V, D> Mache<K, V> create(MacheLoader<K, V, D> cacheLoader, String... options) {
        return new MacheImpl<>(cacheLoader, options);
    }
}
