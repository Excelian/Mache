package org.mache.events;

import java.io.Closeable;
import java.io.IOException;

import javax.jms.JMSException;

import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by sundance on 14/03/15.
 */
public interface MQFactory<K, V extends CoordinationEntryEvent<K>> extends Closeable {
    public BaseCoordinationEntryEventProducer<K, V> getProducer(MQConfiguration config);
    public BaseCoordinationEntryEventConsumer<K, V> getConsumer(MQConfiguration config) throws IOException, JMSException;
    public void close() throws IOException;
}
