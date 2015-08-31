package com.excelian.mache.observable.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryInvalidateListener<K> extends CoordinationEventListener<K> {
    void onInvalidate(Iterable<CoordinationEntryEvent<K>> events)
            throws CacheEntryListenerException;
}
