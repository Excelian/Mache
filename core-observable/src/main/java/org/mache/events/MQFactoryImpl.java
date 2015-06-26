package org.mache.events;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by sundance on 14/03/15.
 */
public class MQFactoryImpl<K, V extends CoordinationEntryEvent<K>> implements  MQFactory<K, V> {
    private BlockingQueue<V> eventQueue;
    
    public MQFactoryImpl(){
        eventQueue = new ArrayBlockingQueue<V>(100);
    }

    @Override
    public BaseCoordinationEntryEventProducer<K, V> getProducer(MQConfiguration config) {
        return new EventProducer<K, V>(eventQueue, config.getTopicName());
    }

    @Override
    public BaseCoordinationEntryEventConsumer<K, V> getConsumer(MQConfiguration config) {
        return new EventConsumer<K, V>(eventQueue, config.getTopicName());
    }

    @Override
    public void close() {

    }
}
