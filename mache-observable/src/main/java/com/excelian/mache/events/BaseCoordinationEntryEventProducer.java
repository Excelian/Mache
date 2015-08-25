package com.excelian.mache.events;

import com.excelian.mache.observable.MapEventListener;

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
