package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryInvalidateListener extends CoordinationEventListener {
    void onInvalidate(Iterable<CoordinationEntryEvent<?>> events)
            throws CacheEntryListenerException;
}
