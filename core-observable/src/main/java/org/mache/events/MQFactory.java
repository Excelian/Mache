package org.mache.events;

import javax.jms.JMSException;
import java.io.Closeable;
import java.io.IOException;

public interface MQFactory extends Closeable {
    public BaseCoordinationEntryEventProducer getProducer(MQConfiguration config);

    public BaseCoordinationEntryEventConsumer getConsumer(MQConfiguration config) throws IOException, JMSException;

    public void close() throws IOException;
}
