package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryCreatedListener<K> extends CoordinationEventListener<K> {
    void onCreated(Iterable<CoordinationEntryEvent<K>> events)
            throws CacheEntryListenerException;
}
