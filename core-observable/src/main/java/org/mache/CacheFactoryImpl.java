package org.mache;

import java.io.IOException;

import javax.cache.event.CacheEntryListenerException;
import javax.jms.JMSException;

import org.mache.coordination.CoordinationEntryEvent;
import org.mache.coordination.RemoteCacheEntryRemovedListener;
import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;

//TODO create artifact to put this class into - it probably will be final artifact depending on anything else
public class CacheFactoryImpl implements CacheFactory {
	private final MQFactory communicationFactory;
	private final MQConfiguration configuration;
	private final CacheThingFactory cacheThingFactory;

	public CacheFactoryImpl(final MQFactory communicationFactory, final MQConfiguration configuration, final CacheThingFactory cacheThingFactory) {
		this.communicationFactory = communicationFactory;
		this.configuration = configuration;
		this.cacheThingFactory = cacheThingFactory;
	}

	@Override
	public <K, V, D> ExCache<K, V> createCache(final ExCacheLoader<K, V, D> cacheLoader) {
		return createCache(cacheLoader, (String[]) null);
	}

	// TODO introduce cacheLoaderFactory after moved to proper artifact
	@Override
	public <K, V, D> ExCache<K, V> createCache(ExCacheLoader<K, V, D> cacheLoader, String... options) {
		final ExCache<K, V> underlyingCache = cacheThingFactory.create(cacheLoader, options);
		final ObservableMap<K, V> observable = new ObservableMap<K, V>(underlyingCache);
		observable.registerListener(communicationFactory.getProducer(configuration));
		try {
			communicationFactory.getConsumer(configuration).registerEventListener(
					new RemoteCacheEntryRemovedListener() {
						@Override
						public void onRemoved(Iterable<CoordinationEntryEvent<?>> events)
								throws CacheEntryListenerException {
							for (final CoordinationEntryEvent<?> e : events) {
								if (e.getEntityName().equals(underlyingCache.getName())) {
									@SuppressWarnings("unchecked")
									K key = (K) e.getKey();
									underlyingCache.remove(key);
								}
							}
						}
					});
		} catch (IOException | JMSException e) {
			throw new RuntimeException("Error creating cache from loader " + cacheLoader + " and options " + options, e);
		}

		return observable;
	}

}
