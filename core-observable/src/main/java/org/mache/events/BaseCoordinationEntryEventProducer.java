package org.mache.events;

import java.io.IOException;

import javax.jms.JMSException;

import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by sundance on 14/03/15.
 */
public abstract class BaseCoordinationEntryEventProducer<K,V extends CoordinationEntryEvent<K>> {

    private String topicName;

    public String getTopicName()
    {
       return topicName;
    }

    protected BaseCoordinationEntryEventProducer(String topicName)
    {
        this.topicName = topicName;
    }

    abstract public void send(V event) throws InterruptedException, IOException, JMSException;
    abstract public void close();
}
