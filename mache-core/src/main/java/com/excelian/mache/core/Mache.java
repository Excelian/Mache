package com.excelian.mache.core;

import java.util.UUID;

/**
 * A Key value cache that may be backed by a pluggable big data store, and may
 * be listened-to for events.
 * <p>Implementations of this interface are expected to be thread-safe, and can be safely accessed
 * by multiple concurrent threads.
 *
 * @param <K> The type of Keys
 * @param <V> The type of Values
 */
public interface Mache<K, V> {
    String getName();

    UUID getId();

    /**
     * Returns the value associated with {@code key} in this cache.
     * If the key does not exist within the map null is returned.
     *
     * @param key The key to load
     * @return The value associated with the {@code key}
     */
    V get(K key);

    void put(K key, V value);

    void remove(K key);

    void invalidateAll();

    void invalidate(K key);

    void close();

    MacheLoader getCacheLoader();
}
