package org.mache.events;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by sundance on 14/03/15.
 */
public class MQFactoryImpl implements  MQFactory {
    private BlockingQueue<CoordinationEntryEvent<?>> eventQueue;
    
    public MQFactoryImpl(){
        eventQueue = new ArrayBlockingQueue<CoordinationEntryEvent<?>>(100);
    }

    @Override
    public BaseCoordinationEntryEventProducer getProducer(MQConfiguration config) {
        return new EventProducer(eventQueue, config.getTopicName());
    }

    @Override
    public BaseCoordinationEntryEventConsumer getConsumer(MQConfiguration config) {
        return new EventConsumer(eventQueue, config.getTopicName());
    }

    @Override
    public void close() {

    }
}
