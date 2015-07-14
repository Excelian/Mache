package org.mache.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryInvalidateListener extends CoordinationEventListener {
    void onInvalidate(Iterable<CoordinationEntryEvent<?>> events)
            throws CacheEntryListenerException;
}
