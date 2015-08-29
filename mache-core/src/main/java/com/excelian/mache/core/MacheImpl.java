package com.excelian.mache.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.ForwardingCache;
import com.google.common.cache.LoadingCache;

import com.fasterxml.uuid.Generators;

import java.util.UUID;


public class MacheImpl<K, V> implements Mache<K, V> {

    private final ForwardingCache<K, V> fwdCache;
    private final MacheLoader<K, V, ?> cacheLoader;
    private final UUID cacheId;

    //XXX weak keys cause invalidation to fail because of using identity function for equivalence see http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/CacheBuilder.html#weakKeys()
    //private volatile String spec = "maximumSize=10000,weakKeys,softValues,expireAfterWrite=1d,expireAfterAccess=1d,recordStats";
    private volatile String spec = "maximumSize=10000,softValues,expireAfterWrite=1d,expireAfterAccess=1d,recordStats";

    private final LoadingCache<K, V> cache;
    private volatile boolean created;

    public MacheImpl(final AbstractCacheLoader<K, V, ?> cacheLoader, String... optionalSpec) {
        this.cacheLoader = cacheLoader;
        cacheLoader.create();

        if (optionalSpec != null && optionalSpec.length > 0) {
            this.spec = optionalSpec[0];
        }

        cache = CacheBuilder.from(spec)
            .recordStats()
            .build(cacheLoader);

        fwdCache = new ForwardingCache<K, V>() {
            @Override
            protected Cache<K, V> delegate() {
                return cache;
            }

            @Override
            public void put(K key, V value) {
                delegate().put(key, value);
            }

            @Override
            public void invalidate(Object key) {
                delegate().invalidate(key);
            }
        };

        cacheId = Generators.nameBasedGenerator().generate(getName() + String.valueOf(this));
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
        //the fwdrCache doesnt expose 'getOrLoad(K, Loader)'
        return cache.getUnchecked(key);
    }

    @Override
    public void put(K key, V value) {
        fwdCache.invalidate(key);
        cacheLoader.put(key, value);
    }

    @Override
    public void remove(K key) {
        cacheLoader.remove(key);
        fwdCache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        fwdCache.invalidateAll();
    }

    @Override
    public void invalidate(K key) {
        fwdCache.invalidate(key);
    }

    @Override
    public void close() {
        cacheLoader.close();
    }

    @Override
    public MacheLoader getCacheLoader() {
        return cacheLoader;
    }
}
