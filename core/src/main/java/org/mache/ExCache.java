package org.mache;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Created by neil.avery on 02/06/2015.
 */
public interface ExCache<K,V> {
    String getName();
    UUID getId();

    V get(K k);

    void put(K k, V v);

    void remove(K k);

    void invalidateAll();

    void invalidate(K k);

    void close();

    ExCacheLoader getCacheLoader();
}
