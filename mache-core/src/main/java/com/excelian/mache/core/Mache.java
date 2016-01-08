package com.excelian.mache.core;

import java.util.UUID;

/**
 * A Key value cache that may be backed by a pluggable big data store, and may
 * be listened-to for events
 *
 * @param <K> The type of Keys
 * @param <V> The type of Values
 */
public interface Mache<K, V> extends AutoCloseable {
    String getName();

    UUID getId();

    V get(K key);

    void put(K key, V value);

    void remove(K key);

    void invalidateAll();

    void invalidate(K key);

    void close();

    MacheLoader getCacheLoader();
}
