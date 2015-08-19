package org.mache.events;

import javax.cache.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class EventDispatcher<K, V> extends AbstractEventDispatcher<K, V> {

    public EventDispatcher() {
        super();
        initEventMapper();
    }

    private void initEventMapper() {
        eventMapper = new ConcurrentHashMap<EventType, ArrayList<CacheEntryListener<K, V>>>();
        /*dispatchEvents = new ConcurrentHashMap<EventType, ArrayList<CacheEntryEvent<K, V>>>();
        dispatchEventCounter = new ConcurrentHashMap<EventType, Integer>();*/


        eventMapper.putIfAbsent(EventType.CREATED, new ArrayList<CacheEntryListener<K, V>>());
        eventMapper.putIfAbsent(EventType.REMOVED, new ArrayList<CacheEntryListener<K, V>>());
        eventMapper.putIfAbsent(EventType.UPDATED, new ArrayList<CacheEntryListener<K, V>>());
        eventMapper.putIfAbsent(EventType.EXPIRED, new ArrayList<CacheEntryListener<K, V>>());

        /*dispatchEventCounter.putIfAbsent(EventType.CREATED,0);
        dispatchEventCounter.putIfAbsent(EventType.REMOVED,0);
        dispatchEventCounter.putIfAbsent(EventType.UPDATED,0);
        dispatchEventCounter.putIfAbsent(EventType.EXPIRED,0);*/
    }

    @Override
    public void registerEventListener(CacheEntryListener<K, V> listener) {
        if (listener instanceof CacheEntryCreatedListener) {
            eventMapper.get(EventType.CREATED).add(listener);
        }

        if (listener instanceof CacheEntryRemovedListener) {
            eventMapper.get(EventType.REMOVED).add(listener);
        }

        if (listener instanceof CacheEntryUpdatedListener) {

            eventMapper.get(EventType.UPDATED).add(listener);
        }
        if (listener instanceof CacheEntryUpdatedListener) {
            eventMapper.get(EventType.EXPIRED).add(listener);
        }
    }

    public void fire(CacheEntryEvent<K, V> event) {
        consumerQueue.add(event);

    }

    @Override
    /* Sequential fire and consume event */
    public void fire() {
        while (dispatchQueue.size() > 0) {
            /*Message transport dequeue message and pass onto consumer */
            CacheEntryEvent<K, V> event = dispatchQueue.poll();
            fire(event);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void consume(CacheEntryEvent<K, V> event) {
        List<CacheEntryEvent<K, V>> events = new ArrayList<CacheEntryEvent<K, V>>();
        EventType eventType = event.getEventType();
        for (CacheEntryListener<K, V> listener : eventMapper.get(eventType)) {
            if (eventType == EventType.CREATED) {
                ((CacheEntryCreatedListener) listener).onCreated(events);
            }
            if (eventType == EventType.REMOVED) {
                ((CacheEntryRemovedListener) listener).onRemoved(events);
            }
            if (eventType == EventType.UPDATED) {
                ((CacheEntryUpdatedListener) listener).onUpdated(events);
            }
            if (eventType == EventType.EXPIRED) {
                ((CacheEntryExpiredListener) listener).onExpired(events);
            }
        }
    }

    public void consume() {
        while (consumerQueue.size() > 0) {
            /*Message transport dequeue message and pass onto consumer */
            CacheEntryEvent<K, V> event = consumerQueue.poll();
            consume(event);
        }

    }

}
