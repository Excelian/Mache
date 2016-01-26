package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

/**
 * Common interface for listener to cache entry invalidation.
 * @param <K> the type of the keys
 */
public interface RemoteCacheEntryInvalidateListener<K> extends CoordinationEventListener<K> {
    void onInvalidate(Iterable<CoordinationEntryEvent<K>> events)
            throws CacheEntryListenerException;
}
