package com.excelian.mache.observable;

import com.excelian.mache.core.Mache;
import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.excelian.mache.observable.coordination.RemoteCacheEntryListener;
import com.excelian.mache.observable.utils.UUIDUtils;

import javax.cache.event.CacheEntryListenerException;

/**
 * Factory to create a {@link ObservableMap} around a {@link Mache} object.
 * @param <K> the cache key type.
 * @param <V> the cache value type.
 */
public class MessageQueueObservableCacheFactory<K, V> implements ObservableCacheFactory<K, V> {
    private final MQFactory<K> communicationFactory;
    private final MQConfiguration configuration;
    private final UUIDUtils uuidUtils;

    /**
     * @param communicationFactory The {@link MQFactory} for the underlying messaging framework.
     * @param configuration The {@link MQConfiguration} for the messaging queue.
     * @param uuidUtils A UUID Generator.
     */
    public MessageQueueObservableCacheFactory(final MQFactory<K> communicationFactory,
                                              final MQConfiguration configuration,
                                              final UUIDUtils uuidUtils) {
        this.communicationFactory = communicationFactory;
        this.configuration = configuration;
        this.uuidUtils = uuidUtils;
    }

    @Override
    public Mache<K, V> createCache(final Mache<K, V> underlyingCache) {
        final ObservableMap<K, V> observable = new ObservableMap<>(communicationFactory, underlyingCache, uuidUtils);

        observable.registerListener(communicationFactory.getProducer(configuration));

        try {
            BaseCoordinationEntryEventConsumer<K> consumer = communicationFactory.getConsumer(configuration);
            consumer.registerEventListener(new RemoteCacheEntryListener<K>() {
                @Override
                public void onCreated(Iterable<CoordinationEntryEvent<K>> events)
                    throws CacheEntryListenerException {
                    handle(events);
                }

                @Override
                public void onInvalidate(Iterable<CoordinationEntryEvent<K>> events)
                    throws CacheEntryListenerException {
                    handle(events);
                }

                @Override
                public void onRemoved(Iterable<CoordinationEntryEvent<K>> events)
                    throws CacheEntryListenerException {
                    handle(events);
                }

                @Override
                public void onUpdated(Iterable<CoordinationEntryEvent<K>> events)
                    throws CacheEntryListenerException {
                    handle(events);
                }

                public void handle(Iterable<CoordinationEntryEvent<K>> events) {
                    for (final CoordinationEntryEvent<K> e : events) {
                        if (e.getEntityName().equals(underlyingCache.getName())
                                && !e.getCacheId().equals(underlyingCache.getId())) {
                            K key = e.getKey();
                            underlyingCache.invalidate(key);//if we called remove it would go to the DB too.
                        }
                    }
                }
            });
            consumer.beginSubscriptionThread();
        } catch (Exception e) {
            throw new RuntimeException("Error creating cache consumer from underlying cache " + underlyingCache, e);
        }
        return observable;
    }

}
