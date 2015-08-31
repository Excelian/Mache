package com.excelian.mache.core;

public class MacheFactory<K, V, D> {
    public Mache<K, V> create(MacheLoader<K, V, D> cacheLoader, String... options) {
        return new MacheImpl<>(cacheLoader, options);
    }
}
