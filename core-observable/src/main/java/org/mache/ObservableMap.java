package org.mache;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by neil.avery on 11/06/2015.
 */
public class ObservableMap<K,V> implements ExCache<K,V> {

    private final ExCache<K, V> delegate;
    private MapEventListener listener;
    
    public ObservableMap(ExCache<K,V> delegate) {
    	this.delegate = delegate;
    }

    public void registerListener(MapEventListener listener) {
        this.listener = listener;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object get(K k) throws ExecutionException {
        return delegate.get(k);
    }

    @Override
    public void put(K k, V v) {
        delegate.put(k, v);

        fireInvalidate(k);
    }

    @Override
    public void remove(K k) {
        delegate.remove(k);
        fireInvalidate(k);
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public ExCacheLoader getCacheLoader() {
        return delegate.getCacheLoader();
    }
    
    private void fireInvalidate(K k) {
    	final CoordinationEntryEvent<K> event = new CoordinationEntryEvent<K>(getName(), k, EventType.INVALIDATE, new Date());
    	listener.send(event);
    }
}
