package org.mache.events;

import org.mache.MapEventListener;
import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by sundance on 14/03/15.
 */
public abstract class BaseCoordinationEntryEventProducer implements MapEventListener {

    private String topicName;

    public String getTopicName()
    {
       return topicName;
    }

    protected BaseCoordinationEntryEventProducer(String topicName)
    {
        this.topicName = topicName;
    }

    abstract public void send(CoordinationEntryEvent<?> event);
    abstract public void close();
}
