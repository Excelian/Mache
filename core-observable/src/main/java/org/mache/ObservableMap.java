package org.mache;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.mache.coordination.CoordinationEntryEvent;
import org.mache.utils.UUIDUtils;

/**
 * Created by neil.avery on 11/06/2015.
 */
public class ObservableMap<K,V> implements ExCache<K,V> {

    private final ExCache<K, V> delegate;
    private final UUIDUtils uuidUtils;
    private MapEventListener listener;
    
    public ObservableMap(final ExCache<K,V> delegate, final UUIDUtils uuidUtils) {
    	this.delegate = delegate;
		this.uuidUtils = uuidUtils;
    }

    public void registerListener(MapEventListener listener) {
        this.listener = listener;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public UUID getId() {
        return delegate.getId();
    }

    @Override
    public V get(K k) {
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
    public void invalidate(K k) {
        delegate.invalidate(k);
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
    	final CoordinationEntryEvent<K> event = new CoordinationEntryEvent<K>(getId(), getName(), k, EventType.INVALIDATE, uuidUtils);
        System.out.println("Firing invalidate from " + this + " - cacheId=" + getId());
    	listener.send(event);
    }
}
