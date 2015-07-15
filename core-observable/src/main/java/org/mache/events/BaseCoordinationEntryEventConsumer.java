package org.mache.events;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;

import org.mache.EventType;
import org.mache.coordination.CoordinationEntryEvent;
import org.mache.coordination.CoordinationEventListener;
import org.mache.coordination.RemoteCacheEntryCreatedListener;
import org.mache.coordination.RemoteCacheEntryInvalidateListener;
import org.mache.coordination.RemoteCacheEntryRemovedListener;
import org.mache.coordination.RemoteCacheEntryUpdatedListener;

public abstract class BaseCoordinationEntryEventConsumer implements Closeable {

    abstract public void beginSubscriptionThread() throws InterruptedException, JMSException, IOException;
    abstract public void close();

    protected ConcurrentHashMap<EventType,ArrayList<CoordinationEventListener>> eventMap;
    private String topicName;

    protected BaseCoordinationEntryEventConsumer(String topicName)
    {
    	//TODO lists need to be threadsafe ?
        eventMap = new ConcurrentHashMap<EventType, ArrayList<CoordinationEventListener>>();
        eventMap.putIfAbsent(EventType.CREATED,new ArrayList<CoordinationEventListener>());
        eventMap.putIfAbsent(EventType.REMOVED,new ArrayList<CoordinationEventListener>());
        eventMap.putIfAbsent(EventType.UPDATED,new ArrayList<CoordinationEventListener>());
        eventMap.putIfAbsent(EventType.INVALIDATE, new ArrayList<CoordinationEventListener>());

        this.topicName = topicName;
    }

    public String getTopicName()
    {
        return topicName;
    }

    public void registerEventListener(CoordinationEventListener listener) {
        if(listener instanceof RemoteCacheEntryCreatedListener){
            eventMap.get(EventType.CREATED).add(listener);
        }

        if(listener instanceof RemoteCacheEntryUpdatedListener){
            eventMap.get(EventType.UPDATED).add(listener);
        }

        if(listener instanceof RemoteCacheEntryRemovedListener){
            eventMap.get(EventType.REMOVED).add(listener);
        }

        if(listener instanceof RemoteCacheEntryInvalidateListener){
            eventMap.get(EventType.INVALIDATE).add(listener);
        }
    }

    protected CoordinationEntryEvent<?> routeEventToListeners(
            ConcurrentHashMap<EventType,ArrayList<CoordinationEventListener>> eventMap, CoordinationEntryEvent<?> event)
    {
        EventType eventType = event.getEventType();

        List<CoordinationEntryEvent<?>> events = new ArrayList<CoordinationEntryEvent<?>>();
        events.add(event);

        for (CoordinationEventListener listener : eventMap.get(eventType)) {
            if (eventType == EventType.CREATED) {
                ((RemoteCacheEntryCreatedListener) listener).onCreated(events);
            }else if (eventType == EventType.REMOVED) {
                ((RemoteCacheEntryRemovedListener) listener).onRemoved(events);
            }else if (eventType == EventType.UPDATED) {
                ((RemoteCacheEntryUpdatedListener) listener).onUpdated(events);
            }else if(eventType == EventType.INVALIDATE) {
                ((RemoteCacheEntryInvalidateListener) listener).onInvalidate(events);
            }else
            {
                System.err.print("Error. Unsupported coordination event type received - "+eventType);
            }

        }
        return event;
    }
}