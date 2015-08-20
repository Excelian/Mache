package com.excelian.mache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class TestEntity {
    String pkey;

    TestEntity(String key) {
        this.pkey = key;
    }
}

class TestEntity2 {
    String pkey;
    String otherValue;

    TestEntity2(String key, String otherValue) {
        this.pkey = key;
        this.otherValue = otherValue;
    }

    @Override
    public String toString() {
        return "TestEntity2 [pkey=" + pkey + ", otherValue=" + otherValue + "]";
    }
}

public class InMemoryCacheLoader<K, V> extends AbstractCacheLoader<K, V, String> {
    private final String cacheName;
    private final Map<K, V> store = new ConcurrentHashMap<K, V>();

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
