package org.mache.events;

import javax.jms.JMSException;

import org.mache.EventType;
import org.mache.coordination.CoordinationEntryEvent;
import org.mache.coordination.CoordinationEventListener;
import org.mache.coordination.RemoteCacheEntryCreatedListener;
import org.mache.coordination.RemoteCacheEntryExpiredListener;
import org.mache.coordination.RemoteCacheEntryRemovedListener;
import org.mache.coordination.RemoteCacheEntryUpdatedListener;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public abstract class BaseCoordinationEntryEventConsumer<K, V extends CoordinationEntryEvent<K>> implements Closeable {

    abstract public void beginSubscriptionThread() throws InterruptedException, JMSException, IOException;
    abstract public void close();

    protected ConcurrentHashMap<EventType,ArrayList<CoordinationEventListener<K>>> eventMap;
    private String topicName;

    protected BaseCoordinationEntryEventConsumer(String topicName)
    {
        eventMap = new ConcurrentHashMap<EventType, ArrayList<CoordinationEventListener<K>>>();
        eventMap.putIfAbsent(EventType.CREATED,new ArrayList<CoordinationEventListener<K>>());
        eventMap.putIfAbsent(EventType.REMOVED,new ArrayList<CoordinationEventListener<K>>());
        eventMap.putIfAbsent(EventType.UPDATED,new ArrayList<CoordinationEventListener<K>>());
        eventMap.putIfAbsent(EventType.EXPIRED, new ArrayList<CoordinationEventListener<K>>());

        this.topicName = topicName;
    }

    public String getTopicName()
    {
        return topicName;
    }

    public void registerEventListener(CoordinationEventListener<K> listener) {
        if(listener instanceof RemoteCacheEntryCreatedListener){
            eventMap.get(EventType.CREATED).add(listener);
        }

        if(listener instanceof RemoteCacheEntryUpdatedListener){
            eventMap.get(EventType.UPDATED).add(listener);
        }

        if(listener instanceof RemoteCacheEntryRemovedListener){
            eventMap.get(EventType.REMOVED).add(listener);
        }

        if(listener instanceof RemoteCacheEntryExpiredListener){
            eventMap.get(EventType.EXPIRED).add(listener);
        }
    }

    protected CoordinationEntryEvent<K> RouteEventToListeners(
            ConcurrentHashMap<EventType,ArrayList<CoordinationEventListener<K>>> eventMap, CoordinationEntryEvent<K> event)
    {
        EventType eventType = event.getEventType();

        List<CoordinationEntryEvent<K>> events = new ArrayList<CoordinationEntryEvent<K>>();
        events.add(event);

        for (CoordinationEventListener<K> listener : eventMap.get(eventType)) {
            if (eventType == EventType.CREATED) {
                ((RemoteCacheEntryCreatedListener<K>) listener).onCreated(events);
            }
            if (eventType == EventType.REMOVED) {
                ((RemoteCacheEntryRemovedListener<K>) listener).onRemoved(events);
            }
            if (eventType == EventType.UPDATED) {
                ((RemoteCacheEntryUpdatedListener<K>) listener).onUpdated(events);
            }
            if (eventType == EventType.EXPIRED) {
                ((RemoteCacheEntryExpiredListener<K>) listener).onExpired(events);
            }
        }
        return event;
    }
}
