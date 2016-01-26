package com.excelian.mache.events;

import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.excelian.mache.observable.coordination.CoordinationEventListener;
import com.excelian.mache.observable.coordination.RemoteCacheEntryCreatedListener;
import com.excelian.mache.observable.coordination.RemoteCacheEntryInvalidateListener;
import com.excelian.mache.observable.coordination.RemoteCacheEntryRemovedListener;
import com.excelian.mache.observable.coordination.RemoteCacheEntryUpdatedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.jms.JMSException;

/**
 * Abstract base class for event consumers.
 *
 * @param <K> the type of the keys on the events
 */
public abstract class BaseCoordinationEntryEventConsumer<K> implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(BaseCoordinationEntryEventConsumer.class);

    public abstract void beginSubscriptionThread() throws InterruptedException, JMSException, IOException;

    public abstract void close();

    private final String topicName;

    private final Queue<RemoteCacheEntryCreatedListener<K>> createdEventConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<RemoteCacheEntryRemovedListener<K>> removedEventConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<RemoteCacheEntryUpdatedListener<K>> updatedEventConsumers = new ConcurrentLinkedQueue<>();
    private final Queue<RemoteCacheEntryInvalidateListener<K>>
        invalidatedEventConsumers = new ConcurrentLinkedQueue<>();

    /**
     * Constructor.
     * @param topicName the topic name.
     */
    protected BaseCoordinationEntryEventConsumer(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    /**
     * Registers the event listener.
     * @param listener the listener to register
     */
    @SuppressWarnings("unchecked")
    public void registerEventListener(CoordinationEventListener<K> listener) {
        if (listener instanceof RemoteCacheEntryCreatedListener) {
            createdEventConsumers.add((RemoteCacheEntryCreatedListener) listener);
        }
        if (listener instanceof RemoteCacheEntryUpdatedListener) {
            updatedEventConsumers.add((RemoteCacheEntryUpdatedListener) listener);
        }
        if (listener instanceof RemoteCacheEntryRemovedListener) {
            removedEventConsumers.add((RemoteCacheEntryRemovedListener) listener);
        }
        if (listener instanceof RemoteCacheEntryInvalidateListener) {
            invalidatedEventConsumers.add((RemoteCacheEntryInvalidateListener) listener);
        }
    }

    protected CoordinationEntryEvent<K> routeEventToListeners(CoordinationEntryEvent<K> event) {
        final List<CoordinationEntryEvent<K>> events = new ArrayList<>();
        events.add(event);

        switch (event.getEventType()) {
            case CREATED:
                routeToCreateConsumers(events);
                break;
            case UPDATED:
                routeToUpdatedConsumers(events);
                break;
            case REMOVED:
                routeToRemoveConsumers(events);
                break;
            case INVALIDATE:
                routeToInvalidateConsumers(events);
                break;
            default:
                throw new IllegalArgumentException("Unknown event type:[" + event.getEventType() + "]");
        }
        return event;
    }

    private void routeToInvalidateConsumers(List<CoordinationEntryEvent<K>> events) {
        for (RemoteCacheEntryInvalidateListener<K> invalidatedEventConsumer : invalidatedEventConsumers) {
            invalidatedEventConsumer.onInvalidate(events);
        }
    }

    private void routeToRemoveConsumers(List<CoordinationEntryEvent<K>> events) {
        for (RemoteCacheEntryRemovedListener<K> removedEventConsumer : removedEventConsumers) {
            removedEventConsumer.onRemoved(events);
        }
    }

    private void routeToCreateConsumers(List<CoordinationEntryEvent<K>> events) {
        for (RemoteCacheEntryCreatedListener<K> createdEventConsumer : createdEventConsumers) {
            createdEventConsumer.onCreated(events);
        }
    }

    private void routeToUpdatedConsumers(List<CoordinationEntryEvent<K>> events) {
        for (RemoteCacheEntryUpdatedListener<K> createdEventConsumer : updatedEventConsumers) {
            createdEventConsumer.onUpdated(events);
        }
    }
}