package org.mache.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryExpiredListener extends CoordinationEventListener {
    void onExpired(Iterable<CoordinationEntryEvent<?>> events)
            throws CacheEntryListenerException;
}
