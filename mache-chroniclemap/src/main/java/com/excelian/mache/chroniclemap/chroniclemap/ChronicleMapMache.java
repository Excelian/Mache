package com.excelian.mache.chroniclemap.chroniclemap;

import com.excelian.mache.chroniclemap.chroniclemap.solr.ConcurrentLRUCache;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.fasterxml.uuid.Generators;

import java.util.UUID;

public class ChronicleMapMache<K, V> implements Mache<K, V> {

    private final MacheLoader<K, V, ?> cacheLoader;
    private final UUID cacheId;
    private final ConcurrentLRUCache<K, V> cache;

    public ChronicleMapMache(MacheLoader<K, V, ?> cacheLoader, ConcurrentLRUCache<K, V> cache) {
        this.cacheLoader = cacheLoader;
        cacheId = Generators.nameBasedGenerator().generate(getName() + toString());
        this.cache = cache;
    }

    @Override
    public String getName() {
        return cacheLoader.getName();
    }

    @Override
    public UUID getId() {
        return cacheId;
    }

    @Override
    public V get(final K key) {
        V value = cache.get(key);
        if (value == null) {
            try {
                value = cacheLoader.load(key);
                if (value != null) {
                    cache.put(key, value);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error occurred looking up key: " + key, e);
            }
        }
        return value;
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        cacheLoader.put(key, value);
    }

    @Override
    public void remove(K key) {
        cacheLoader.remove(key);
        cache.remove(key);
    }

    @Override
    public void invalidateAll() {
        cache.clear();
    }

    @Override
    public void invalidate(K key) {
        cache.remove(key);
    }

    @Override
    public void close() {
        cache.close();
        cacheLoader.close();
    }

    @Override
    public MacheLoader getCacheLoader() {
        return cacheLoader;
    }

}
