package com.excelian.mache.observable.coordination;

public interface RemoteCacheEntryListener<K> extends RemoteCacheEntryRemovedListener<K>,
    RemoteCacheEntryInvalidateListener<K>,
    RemoteCacheEntryCreatedListener<K>,
    RemoteCacheEntryUpdatedListener<K> {

}
