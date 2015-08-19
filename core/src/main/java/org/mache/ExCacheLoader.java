package org.mache;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public interface ExCacheLoader<K, V, D> extends RemovalListener<K, V> {

    void create(String name, K k);

    void put(K k, V v);

    void remove(K k);

    V load(K key) throws Exception;

    void onRemoval(RemovalNotification<K, V> notification);

    void close();

    String getName();

    D getDriverSession();
}
