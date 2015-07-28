package org.mache;

import com.google.common.cache.*;

import java.util.concurrent.ExecutionException;


public class CacheThing<K,V> implements ExCache<K,V>  {

    private ForwardingCache<K, V> fwdCache;
    final private ExCacheLoader cacheLoader;

    private String spec = "maximumSize=10000,expireAfterWrite=1d,expireAfterAccess=1d";

    private LoadingCache<K, V> cache;
    private volatile boolean created;

    public CacheThing(final ExCacheLoader cacheLoader, String... optionalSpec) {
        this.cacheLoader = cacheLoader;
        if (optionalSpec != null && optionalSpec.length > 0) this.spec = optionalSpec[0];

        bindToGuavaCache(cacheLoader);
    }

    private void bindToGuavaCache(ExCacheLoader cacheHook) {
        cache = CacheBuilder.from(spec).
                recordStats().removalListener((RemovalListener<K,V>) cacheHook).
                build((CacheLoader<K,V>) cacheHook);

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
    }

    @Override
    public String getName() {
        return cacheLoader.getName();
    }

    @Override
    public V get(final K k) throws ExecutionException {
        createMaybe(k);
        // a bit crappy - but the fwdrCache doesnt expose 'getOrLoad(K, Loader)'
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
    public ExCacheLoader getCacheLoader() {
        return cacheLoader;
    }
}
