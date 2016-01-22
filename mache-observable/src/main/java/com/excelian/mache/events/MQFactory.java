package com.excelian.mache.events;

import java.io.Closeable;
import java.io.IOException;
import javax.jms.JMSException;

/**
 * Type to specify the contract for message queue factories for the different
 * supported messaging platforms.
 * @param <K> the type of the keys
 */
public interface MQFactory<K> extends Closeable {
    public BaseCoordinationEntryEventProducer<K> getProducer(MQConfiguration config);

    public BaseCoordinationEntryEventConsumer<K> getConsumer(MQConfiguration config) throws IOException, JMSException;

    public void close() throws IOException;
}
