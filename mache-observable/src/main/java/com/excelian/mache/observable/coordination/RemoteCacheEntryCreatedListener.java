package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryCreatedListener extends CoordinationEventListener {
    void onCreated(Iterable<CoordinationEntryEvent<?>> events)
            throws CacheEntryListenerException;
}
