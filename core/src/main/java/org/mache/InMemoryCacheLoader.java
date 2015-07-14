package org.mache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCacheLoader<K,V> extends AbstractCacheLoader<String, String, String> {
	private final String cacheName;
	private final Map<String, String> store = new ConcurrentHashMap<String, String>();
	
	public InMemoryCacheLoader(final String name) {
		this.cacheName = name;
	}

	@Override
	public void create(String name, String k) {
	}

	@Override
	public void put(final String k, final String v) {
		store.put(k, v);
	}

	@Override
	public void remove(final String k) {
		store.remove(k);
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public String getName() {
		return cacheName;
	}

	@Override
	public String getDriverSession() {
		throw new UnsupportedOperationException("Not implemented.");
	}

	@Override
	public String load(String key) throws Exception {
		String result = store.get(key);
		
		if (result == null) {
			throw new RuntimeException("Item not found in store.");
		}
		
		return result;
	}

}
