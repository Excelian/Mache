package com.excelian.mache.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryCreatedListener extends CoordinationEventListener {
    void onCreated(Iterable<CoordinationEntryEvent<?>> events)
            throws CacheEntryListenerException;
}
