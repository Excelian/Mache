package com.excelian.mache.core;

import java.util.UUID;

public interface Mache<K, V> {
    String getName();

    UUID getId();

    V get(K k);

    void put(K k, V v);

    void remove(K k);

    void invalidateAll();

    void invalidate(K k);

    void close();

    MacheLoader getCacheLoader();
}
