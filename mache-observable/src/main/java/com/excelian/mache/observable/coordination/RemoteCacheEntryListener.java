package com.excelian.mache.observable.coordination;


/**
 * Common interface for remote cache listeners.
 * @param <K> the type of the keys
 */
public interface RemoteCacheEntryListener<K> extends RemoteCacheEntryRemovedListener<K>,
    RemoteCacheEntryInvalidateListener<K>,
    RemoteCacheEntryCreatedListener<K>,
    RemoteCacheEntryUpdatedListener<K> {

}
