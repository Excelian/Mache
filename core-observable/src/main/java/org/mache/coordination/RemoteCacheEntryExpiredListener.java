package org.mache.coordination;

import javax.cache.event.CacheEntryListenerException;

public interface RemoteCacheEntryExpiredListener<K> extends CoordinationEventListener<K> {
    void onExpired(Iterable<CoordinationEntryEvent<K>> events)
            throws CacheEntryListenerException;
}
