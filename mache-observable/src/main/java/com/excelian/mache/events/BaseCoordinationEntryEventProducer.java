package com.excelian.mache.events;

import com.excelian.mache.observable.MapEventListener;

/**
 * Base class for event producers.
 *
 * @param <K> the type of the keys
 */
public abstract class BaseCoordinationEntryEventProducer<K> implements MapEventListener<K> {

    private String topicName;

    public String getTopicName() {
        return topicName;
    }

    protected BaseCoordinationEntryEventProducer(String topicName) {
        this.topicName = topicName;
    }

    public abstract void close();
}
