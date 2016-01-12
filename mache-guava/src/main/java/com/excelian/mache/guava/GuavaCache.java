package com.excelian.mache.guava;

import com.excelian.mache.core.Cache;
import com.excelian.mache.core.MacheLoader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;

/**
 * Created by jbowkett on 11/01/2016.
 */
public class GuavaCache<K, V> implements Cache<K, V> {

    //XXX weak keys cause invalidation to fail because of using identity function for equivalence see http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/cache/CacheBuilder.html#weakKeys()
    //private volatile String spec = "maximumSize=10000,weakKeys,softValues,expireAfterWrite=1d,expireAfterAccess=1d,recordStats";
    private String DEFAULT_SPEC = "maximumSize=10000,softValues,expireAfterWrite=1d,expireAfterAccess=1d,recordStats";

    private LoadingCache<K, V> cache;

    @Override
    public void create(MacheLoader<K, V, ?> cacheLoader, String... optionalSpec) {
        final String spec = getSpec(optionalSpec);
        cache = CacheBuilder.from(spec)
            .recordStats()
            .build(new GuavaCacheLoaderAdapter<>(cacheLoader));
    }

    private String getSpec(String[] optionalSpec) {
        final String spec;
        if (optionalSpec != null && optionalSpec.length > 0) {
            spec = optionalSpec[0];
        } else {
            spec = DEFAULT_SPEC;
        }
        return spec;
    }

    @Override
    public V getUnchecked(K key) {
        return cache.getUnchecked(key);
    }

    @Override
    public void invalidate(K key) {
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }
}
