package com.excelian.mache.core;

/**
 * Created by jbowkett on 11/01/2016.
 */
public interface Cache<K, V> {

    void create(MacheLoader<K, V, ?> cacheLoader, String... optionalSpec);

    V getUnchecked(K key);

    void invalidate(K key);

    void invalidateAll();

    void put(K key, V value);
}
