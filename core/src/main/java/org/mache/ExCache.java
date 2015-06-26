package org.mache;

import java.util.concurrent.ExecutionException;

/**
 * Created by neil.avery on 02/06/2015.
 */
public interface ExCache<K,V> {
    String getName();

    Object get(K k) throws ExecutionException;

    void put(K k, V v);

    void remove(K k);

    void invalidateAll();

    void close();

    ExCacheLoader getCacheLoader();
}
