package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

/**
 * Common interface for listener to cache entry update.
 * @param <K> the type of the keys
 */
public interface RemoteCacheEntryUpdatedListener<K> extends CoordinationEventListener<K> {
    void onUpdated(Iterable<CoordinationEntryEvent<K>> events)
            throws CacheEntryListenerException;
}
