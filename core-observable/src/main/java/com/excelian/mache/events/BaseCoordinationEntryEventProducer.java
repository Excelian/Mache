package com.excelian.mache.events;

import com.excelian.mache.MapEventListener;
import com.excelian.mache.coordination.CoordinationEntryEvent;

public abstract class BaseCoordinationEntryEventProducer implements MapEventListener {

    private String topicName;

    public String getTopicName() {
        return topicName;
    }

    protected BaseCoordinationEntryEventProducer(String topicName) {
        this.topicName = topicName;
    }

    abstract public void send(CoordinationEntryEvent<?> event);

    abstract public void close();
}
