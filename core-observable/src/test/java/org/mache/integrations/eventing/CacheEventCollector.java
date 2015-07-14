package org.mache.integrations.eventing;

import org.mache.coordination.*;

import javax.cache.event.CacheEntryListenerException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CacheEventCollector<K> implements RemoteCacheEntryCreatedListener, RemoteCacheEntryUpdatedListener,RemoteCacheEntryRemovedListener,RemoteCacheEntryExpiredListener {

    BlockingQueue<CoordinationEntryEvent<K>> queue = new ArrayBlockingQueue<CoordinationEntryEvent<K>>(1);

    public CoordinationEntryEvent<K> pollWithTimeout() throws InterruptedException {
        return queue.poll(5, TimeUnit.SECONDS);
    }

    public CoordinationEntryEvent<K> pollWithTimeout(long msecs) throws InterruptedException {
        return queue.poll(msecs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onCreated(Iterable iterable) throws CacheEntryListenerException {
        for(CoordinationEntryEvent<K> evt: (Iterable<CoordinationEntryEvent<K>>) iterable){
            queue.add(evt);
        }
    }

    @Override
    public void onExpired(Iterable iterable) throws CacheEntryListenerException {
        for(CoordinationEntryEvent<K> evt: (Iterable<CoordinationEntryEvent<K>>) iterable){
            queue.add(evt);
        }
    }

    @Override
    public void onRemoved(Iterable iterable) throws CacheEntryListenerException {
        for(CoordinationEntryEvent<K> evt: (Iterable<CoordinationEntryEvent<K>>) iterable){
            queue.add(evt);
        }
    }

    @Override
    public void onUpdated(Iterable iterable) throws CacheEntryListenerException {
        for(CoordinationEntryEvent<K> evt: (Iterable<CoordinationEntryEvent<K>>) iterable){
            queue.add(evt);
        }
    }
}

