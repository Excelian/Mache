package com.excelian.mache.core;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public interface MacheLoader<K, V, D> extends RemovalListener<K, V> {

    void create();

    void put(K k, V v);

    void remove(K k);

    V load(K key) throws Exception;

    void onRemoval(RemovalNotification<K, V> notification);

    void close();

    String getName();

    D getDriverSession();
}
