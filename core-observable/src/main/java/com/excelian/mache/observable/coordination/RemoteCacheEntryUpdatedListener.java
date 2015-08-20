package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryUpdatedListener extends CoordinationEventListener {
    void onUpdated(Iterable<CoordinationEntryEvent<?>> events)
            throws CacheEntryListenerException;
}
