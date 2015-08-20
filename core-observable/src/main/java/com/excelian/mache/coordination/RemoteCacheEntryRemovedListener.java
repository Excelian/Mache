package com.excelian.mache.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryRemovedListener extends CoordinationEventListener {
    void onRemoved(Iterable<CoordinationEntryEvent<?>> events)
            throws CacheEntryListenerException;
}


