package com.excelian.mache.events;


import com.excelian.mache.observable.coordination.CoordinationEntryEvent;

import java.util.concurrent.BlockingQueue;

public class EventProducer<K> extends BaseCoordinationEntryEventProducer<K> {
    private final BlockingQueue<CoordinationEntryEvent<?>> eventQueue;

    public EventProducer(BlockingQueue<CoordinationEntryEvent<?>> queue, String topicName) {
        super(topicName);
        eventQueue = queue;
    }

    @Override
    public void send(CoordinationEntryEvent<K> event) {
        try {
            eventQueue.put(event);
        } catch (final InterruptedException e) {
            throw new RuntimeException("Error while putting message into queue.", e);
        }
    }

    @Override
    public void close() {

    }
}
