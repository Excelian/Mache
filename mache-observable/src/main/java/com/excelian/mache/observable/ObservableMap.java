package com.excelian.mache.observable;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.excelian.mache.observable.utils.UUIDUtils;

import java.util.UUID;

/**
 * Wraps the map interface and allows for events on the underlying cache to be
 * listened to for changes to its contents.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 * @see
 */
public class ObservableMap<K, V> implements Mache<K, V> {

    private final Mache<K, V> delegate;
    private final UUIDUtils uuidUtils;
    private MapEventListener<K> listener;

    public ObservableMap(final Mache<K, V> delegate, final UUIDUtils uuidUtils) {
        this.delegate = delegate;
        this.uuidUtils = uuidUtils;
    }

    public void registerListener(MapEventListener<K> listener) {
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
    public V get(K key) {
        return delegate.get(key);
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, value);

        fireInvalidate(key);
    }

    @Override
    public void remove(K key) {
        delegate.remove(key);
        fireInvalidate(key);
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
    }

    @Override
    public void invalidate(K key) {
        delegate.invalidate(key);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public MacheLoader getCacheLoader() {
        return delegate.getCacheLoader();
    }

    private void fireInvalidate(K key) {
        final CoordinationEntryEvent<K> event = new CoordinationEntryEvent<K>(getId(),
            getName(), key, EventType.INVALIDATE, uuidUtils);
        listener.send(event);
    }
}
