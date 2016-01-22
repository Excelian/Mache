package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

/**
 * Common interface for listener to cache entry created.
 * @param <K> the type of the keys
 */
public interface RemoteCacheEntryCreatedListener<K> extends CoordinationEventListener<K> {
    void onCreated(Iterable<CoordinationEntryEvent<K>> events)
            throws CacheEntryListenerException;
}
