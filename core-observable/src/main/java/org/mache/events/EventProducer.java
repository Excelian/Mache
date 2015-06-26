package org.mache.events;



import java.util.concurrent.BlockingQueue;

import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by sundance on 14/03/15.
 */
public class EventProducer  extends BaseCoordinationEntryEventProducer {
    private final BlockingQueue<CoordinationEntryEvent<?>> eventQueue;

    public EventProducer(BlockingQueue<CoordinationEntryEvent<?>> queue, String topicName) {
        super(topicName);
        eventQueue=queue;
    }

    @Override
    public void send(CoordinationEntryEvent<?> event) {
        try {
			eventQueue.put(event);
		} catch (final InterruptedException e) {
			throw new RuntimeException("Error while putting message into queue.", e);
		}
    }

    @Override
    public void close() {

    }
}
