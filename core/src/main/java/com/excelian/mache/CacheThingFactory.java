package com.excelian.mache;

public class CacheThingFactory {
    public <K, V, D> ExCache<K, V> create(ExCacheLoader<K, V, D> cacheLoader, String... options) {
        return new CacheThing<K, V>(cacheLoader, options);
    }
}
