package org.mache.events;

import java.io.Closeable;
import java.io.IOException;

import javax.jms.JMSException;

/**
 * Created by sundance on 14/03/15.
 */
public interface MQFactory extends Closeable {
    public BaseCoordinationEntryEventProducer getProducer(MQConfiguration config);
    public BaseCoordinationEntryEventConsumer getConsumer(MQConfiguration config) throws IOException, JMSException;
    public void close() throws IOException;
}
