package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

/**
 * Common interface for listener to remote cache eviction.
 * @param <K> the type of the keys
 */
public interface RemoteCacheEntryRemovedListener<K> extends CoordinationEventListener<K> {
    void onRemoved(Iterable<CoordinationEntryEvent<K>> events)
            throws CacheEntryListenerException;
}


