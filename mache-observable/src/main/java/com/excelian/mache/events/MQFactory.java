package com.excelian.mache.events;

import java.io.Closeable;
import java.io.IOException;
import javax.jms.JMSException;

public interface MQFactory<K> extends Closeable {
    public BaseCoordinationEntryEventProducer<K> getProducer(MQConfiguration config);

    public BaseCoordinationEntryEventConsumer<K> getConsumer(MQConfiguration config) throws IOException, JMSException;

    public void close() throws IOException;
}
