package com.excelian.mache.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HashMapMache<K, V> implements Mache<K, V> {
    Map<K, V> backingMap = new HashMap<>();
    final MacheLoader<K, V> loader;

    public HashMapMache(MacheLoader<K, V> loader) {
        this.loader = loader;
    }


    @Override
    public String getName() {
        return "HashMapMache";
    }

    @Override
    public UUID getId() {
        return UUID.fromString(getName() + toString());
    }

    @Override
    public V get(K key) {
        return backingMap.get(key);
    }

    @Override
    public void put(K key, V value) {
        backingMap.put(key, value);
    }

    @Override
    public void remove(K key) {
        backingMap.remove(key);
    }

    @Override
    public void invalidateAll() {
        backingMap.clear();
    }

    @Override
    public void invalidate(K key) {
        backingMap.remove(key);
    }

    @Override
    public void close() {
        backingMap.clear();
    }

    @Override
    public MacheLoader<K, V> getCacheLoader() {
        return null;
    }
}
