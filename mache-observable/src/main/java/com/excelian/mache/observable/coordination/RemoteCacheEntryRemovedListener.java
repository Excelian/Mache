package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryRemovedListener<K> extends CoordinationEventListener<K> {
    void onRemoved(Iterable<CoordinationEntryEvent<K>> events)
            throws CacheEntryListenerException;
}


