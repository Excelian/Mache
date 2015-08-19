package org.mache.events;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.EventType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractEventDispatcher<K, V> implements EventDispatcherIF<K, V> {
    ConcurrentLinkedQueue<CacheEntryEvent<K, V>> dispatchQueue;
    ConcurrentLinkedQueue<CacheEntryEvent<K, V>> consumerQueue;

    protected ConcurrentHashMap<EventType, ArrayList<CacheEntryListener<K, V>>> eventMapper;

    public AbstractEventDispatcher() {
        dispatchQueue = new ConcurrentLinkedQueue<CacheEntryEvent<K, V>>();
        consumerQueue = new ConcurrentLinkedQueue<CacheEntryEvent<K, V>>();
    }

    @Override
    public void addEvent(CacheEntryEvent<K, V> event) {
        dispatchQueue.add(event);
    }

    @Override
    public List<? extends CacheEntryEvent<K, V>> getQueuedEvents() {
        List<CacheEntryEvent<K, V>> events = new ArrayList<CacheEntryEvent<K, V>>();
        Iterator<CacheEntryEvent<K, V>> iterator = dispatchQueue.iterator();
        while (iterator.hasNext()) {
            events.add(iterator.next());
        }
        return events;
    }


}
