package com.excelian.mache.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCacheLoader<K, V> extends AbstractCacheLoader<K, V, String> {
    private final String cacheName;
    private final Map<K, V> store = new ConcurrentHashMap<>();

    public InMemoryCacheLoader(final String name) {
        this.cacheName = name;
    }


    @Override
    public void create(String name, K k) {

    }

    @Override
    public void put(final K k, final V v) {
        store.put(k, v);
    }

    @Override
    public void remove(final K k) {
        store.remove(k);
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public String getDriverSession() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public V load(K key) throws Exception {
        V result = store.get(key);

        if (result == null) {
            throw new RuntimeException("Item not found in store.");
        }

        return result;
    }
}
