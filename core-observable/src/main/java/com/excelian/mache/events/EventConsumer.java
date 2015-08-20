package com.excelian.mache.events;

import com.excelian.mache.coordination.CoordinationEntryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventConsumer extends BaseCoordinationEntryEventConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(EventConsumer.class);
    private final BlockingQueue<CoordinationEntryEvent<?>> eventQueue;
    private final ExecutorService executor = Executors
            .newSingleThreadExecutor();

    public EventConsumer(final BlockingQueue<CoordinationEntryEvent<?>> queue, final String topicName) {
        super(topicName);
        eventQueue = queue;
    }

    @Override
    public void beginSubscriptionThread() throws InterruptedException,
            JMSException {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        CoordinationEntryEvent<?> event = eventQueue.take();
                        LOG.info("[XEventConsumer] take - CacheEntryEvent: {}", event.getEventType());
                        routeEventToListeners(eventMap, event);

                    } catch (InterruptedException e) {
                        LOG.error("[XCache] eventConsumer - could not 'take' event");
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
