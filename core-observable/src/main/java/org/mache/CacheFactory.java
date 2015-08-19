package org.mache;

public interface CacheFactory {
    <K, V, D> ExCache<K, V> createCache(ExCacheLoader<K, V, D> cacheLoader);

    <K, V, D> ExCache<K, V> createCache(ExCacheLoader<K, V, D> cacheLoader, String... options);
}
