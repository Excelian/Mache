package org.mache;

import org.mache.coordination.CoordinationEntryEvent;
import org.mache.coordination.RemoteCacheEntryListener;
import org.mache.events.BaseCoordinationEntryEventConsumer;
import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;
import org.mache.utils.UUIDUtils;

import javax.cache.event.CacheEntryListenerException;

//TODO create artifact to put this class into - it probably will be final artifact depending on anything else
public class CacheFactoryImpl implements CacheFactory {
    private final MQFactory communicationFactory;
    private final MQConfiguration configuration;
    private final CacheThingFactory cacheThingFactory;
    private final UUIDUtils uuidUtils;

    public CacheFactoryImpl(final MQFactory communicationFactory, final MQConfiguration configuration, final CacheThingFactory cacheThingFactory, final UUIDUtils uuidUtils) {
        this.communicationFactory = communicationFactory;
        this.configuration = configuration;
        this.cacheThingFactory = cacheThingFactory;
        this.uuidUtils = uuidUtils;
    }

    @Override
    public <K, V, D> ExCache<K, V> createCache(final ExCacheLoader<K, V, D> cacheLoader) {
        return createCache(cacheLoader, (String[]) null);
    }


    // TODO introduce cacheLoaderFactory after moved to proper artifact
    @Override
    public <K, V, D> ExCache<K, V> createCache(ExCacheLoader<K, V, D> cacheLoader, String... options) {
        final ExCache<K, V> underlyingCache = cacheThingFactory.create(cacheLoader, options);
        final ObservableMap<K, V> observable = new ObservableMap<K, V>(underlyingCache, uuidUtils);

        observable.registerListener(communicationFactory.getProducer(configuration));

        try {
            BaseCoordinationEntryEventConsumer consumer = communicationFactory.getConsumer(configuration);
            consumer.registerEventListener(
                    new RemoteCacheEntryListener() {
                        @Override
                        public void onUpdated(Iterable<CoordinationEntryEvent<?>> events) throws CacheEntryListenerException {
                            handle(events);
                        }

                        @Override
                        public void onInvalidate(Iterable<CoordinationEntryEvent<?>> events) throws CacheEntryListenerException {
                            handle(events);
                        }

                        @Override
                        public void onCreated(Iterable<CoordinationEntryEvent<?>> events) throws CacheEntryListenerException {
                            handle(events);
                        }

                        @Override
                        public void onRemoved(Iterable<CoordinationEntryEvent<?>> events)
                                throws CacheEntryListenerException {
                            handle(events);
                        }

                        public void handle(Iterable<CoordinationEntryEvent<?>> events) {
                            for (final CoordinationEntryEvent<?> e : events) {
                                if (e.getEntityName().equals(underlyingCache.getName()) && !e.getCacheId().equals(underlyingCache.getId())) {
                                    @SuppressWarnings("unchecked")
                                    K key = (K) e.getKey();
                                    underlyingCache.invalidate(key);//if we called remove it would go to the DB too.
                                }
                            }
                        }
                    });
            consumer.beginSubscriptionThread();
        } catch (Exception e) {
            throw new RuntimeException("Error creating cache consumer from loader " + cacheLoader + " and options " + options, e);
        }
        return observable;
    }

}
