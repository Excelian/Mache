package com.excelian.mache.integrations.eventing;

import com.excelian.mache.observable.coordination.RemoteCacheEntryCreatedListener;
import com.excelian.mache.observable.coordination.RemoteCacheEntryInvalidateListener;
import com.excelian.mache.observable.coordination.RemoteCacheEntryRemovedListener;
import com.excelian.mache.observable.coordination.RemoteCacheEntryUpdatedListener;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;

import javax.cache.event.CacheEntryListenerException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class CacheEventCollector<K> implements RemoteCacheEntryCreatedListener<K>,
    RemoteCacheEntryUpdatedListener<K>,
    RemoteCacheEntryRemovedListener<K>,
    RemoteCacheEntryInvalidateListener<K> {

    private final BlockingQueue<CoordinationEntryEvent<K>> queue = new ArrayBlockingQueue<>(1);

    public CoordinationEntryEvent<K> pollWithTimeout(long msecs) throws InterruptedException {
        return queue.poll(msecs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onCreated(Iterable<CoordinationEntryEvent<K>> events) throws CacheEntryListenerException {
        addAllToQueue(events);
    }

    @Override
    public void onInvalidate(Iterable<CoordinationEntryEvent<K>> events) throws CacheEntryListenerException {
        addAllToQueue(events);
    }

    @Override
    public void onRemoved(Iterable<CoordinationEntryEvent<K>> events) throws CacheEntryListenerException {
        addAllToQueue(events);
    }

    @Override
    public void onUpdated(Iterable<CoordinationEntryEvent<K>> events) throws CacheEntryListenerException {
        addAllToQueue(events);
    }

    private void addAllToQueue(Iterable<CoordinationEntryEvent<K>> events) {
        for (CoordinationEntryEvent<K> evt : events) {
            queue.add(evt);
        }
    }
}

