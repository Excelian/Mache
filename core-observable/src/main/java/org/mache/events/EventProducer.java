package org.mache.events;



import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by sundance on 14/03/15.
 */
public class EventProducer<K, V extends CoordinationEntryEvent<K>>  extends BaseCoordinationEntryEventProducer<K, V> {
    private final BlockingQueue<V> eventQueue;

    public EventProducer(BlockingQueue<V> queue, String topicName) {
        super(topicName);
        eventQueue=queue;
    }

    @Override
    public void send(V event) throws InterruptedException, IOException {
        eventQueue.put(event);
    }

    @Override
    public void close() {

    }
}
