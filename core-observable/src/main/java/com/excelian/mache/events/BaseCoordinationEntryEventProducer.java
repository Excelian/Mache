package com.excelian.mache.events;

import com.excelian.mache.observable.MapEventListener;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;

public abstract class BaseCoordinationEntryEventProducer implements MapEventListener {

    private String topicName;

    public String getTopicName() {
        return topicName;
    }

    protected BaseCoordinationEntryEventProducer(String topicName) {
        this.topicName = topicName;
    }

    public abstract void send(CoordinationEntryEvent<?> event);

    public abstract void close();
}
