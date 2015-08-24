package com.excelian.mache.observable;

import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.observable.utils.UUIDUtils;

import java.util.UUID;

public class ObservableMap<K, V> implements Mache<K, V> {

    private final Mache<K, V> delegate;
    private final UUIDUtils uuidUtils;
    private MapEventListener listener;

    public ObservableMap(final Mache<K, V> delegate, final UUIDUtils uuidUtils) {
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
    public MacheLoader getCacheLoader() {
        return delegate.getCacheLoader();
    }

    private void fireInvalidate(K k) {
        final CoordinationEntryEvent<K> event = new CoordinationEntryEvent<K>(getId(), getName(), k, EventType.INVALIDATE, uuidUtils);
        listener.send(event);
    }
}
