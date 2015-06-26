package org.mache.events;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.JMSException;

import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by sundance on 14/03/15.
 */
public class EventConsumer<K,V extends CoordinationEntryEvent<K>> extends BaseCoordinationEntryEventConsumer<K,V> {
	private final BlockingQueue<V> eventQueue;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public EventConsumer(final BlockingQueue<V> queue, final String topicName) {
        super(topicName);
        eventQueue=queue;
    }

    @Override
    public void beginSubscriptionThread() throws InterruptedException, JMSException {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        CoordinationEntryEvent<K> event=eventQueue.take();
                        System.out.println("[XEventConsumer] take - CacheEntryEvent:" + event.getEventType().toString());
                        RouteEventToListeners( eventMap, event);

                    } catch (Exception e) {
                        System.out.println("[XCache] eventConsumer - could not 'take' event");
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
