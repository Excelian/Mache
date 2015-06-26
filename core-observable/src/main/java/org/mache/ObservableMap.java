package org.mache;

import java.util.EventListener;
import java.util.concurrent.ExecutionException;

/**
 * Created by neil.avery on 11/06/2015.
 */
public class ObservableMap<K,V> implements ExCache<K,V> {

    private final ExCache<K, V> delegate;
    private MapEventListener listener;


    public void registerListener(MapEventListener listener) {
        this.listener = listener;
    }

    public ObservableMap(ExCache<K,V> delegate) {
        this.delegate = delegate;
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
        listener.fire(EventType.INVALIDATE);
    }

    @Override
    public void remove(K k) {
        delegate.remove(k);
        listener.fire(EventType.INVALIDATE);
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
}
