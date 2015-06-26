package org.mache;

import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;

//TODO create artifact to put this class into - it probably will be final artifact depending on anything else
public class CacheFactoryImpl implements CacheFactory {
	private final MQFactory communicationFactory;
	private MQConfiguration configuration;

	public CacheFactoryImpl(final MQFactory communicationFactory, final MQConfiguration configuration) {
		this.communicationFactory = communicationFactory;
		this.configuration = configuration;
	}
	
	//TODO introduce cacheLoaderFactory after moved to proper artifact
	@Override
	public <K, V, D> ExCache<K, V> createCache(
			ExCacheLoader<K, V, D> cacheLoader, String... options) {
		final CacheThing<K, V> underlyingCache = new CacheThing<K, V>(cacheLoader, options);
		final ObservableMap<K, V> observable = new ObservableMap<K, V>(underlyingCache);
		observable.registerListener(communicationFactory.getProducer(configuration));
		
		return observable;
	}

}
