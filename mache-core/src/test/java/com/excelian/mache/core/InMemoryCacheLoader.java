package com.excelian.mache.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCacheLoader<K, V, D> extends AbstractCacheLoader<K, V, Object> {
    private final String cacheName;
    private final Map<K, V> store = new ConcurrentHashMap<>();

    public InMemoryCacheLoader(final Class<V> valueType) {
        this.cacheName = valueType.getSimpleName();
    }


    @Override
    public void create() {
        // NOOP
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
        // NOOP
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
