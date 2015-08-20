package com.excelian.mache.core;

import com.fasterxml.uuid.Generators;
import com.google.common.cache.*;

import java.util.UUID;


public class MacheImpl<K, V> implements Mache<K, V> {

    private final ForwardingCache<K, V> fwdCache;
    final private MacheLoader<K, V, ?> cacheLoader;
    private final UUID cacheId;

    //XXX weak keys cause invalidation tto fail because of using identity function for quivalence see http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/CacheBuilder.html#weakKeys()
    //private volatile String spec = "maximumSize=10000,weakKeys,softValues,expireAfterWrite=1d,expireAfterAccess=1d,recordStats";
    private volatile String spec = "maximumSize=10000,softValues,expireAfterWrite=1d,expireAfterAccess=1d,recordStats";

    private final LoadingCache<K, V> cache;
    private volatile boolean created;

    public MacheImpl(final MacheLoader<K, V, ?> cacheLoader, String... optionalSpec) {
        this.cacheLoader = cacheLoader;
        if (optionalSpec != null && optionalSpec.length > 0) this.spec = optionalSpec[0];

        cache = CacheBuilder.from(spec)
                .recordStats()
                .build((CacheLoader<K, V>) cacheLoader);

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
    public V get(final K k) {
        createMaybe(k);
        //the fwdrCache doesnt expose 'getOrLoad(K, Loader)'
        return cache.getUnchecked(k);
    }

    @Override
    public void put(K k, V v) {

        createMaybe(k);
        fwdCache.invalidate(k);
        cacheLoader.put(k, v);
    }

    @Override
    public void remove(K k) {
        createMaybe(k);
        cacheLoader.remove(k);
        fwdCache.invalidate(k);
    }

    @Override
    public void invalidateAll() {
        fwdCache.invalidateAll();

    }

    @Override
    public void invalidate(K k) {
        fwdCache.invalidate(k);
    }

    private void createMaybe(K k) {
        if (!created) {
            synchronized (this) {
                if (!created) {
                    cacheLoader.create(cacheLoader.getName(), k);
                    created = true;
                }
            }
        }
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
