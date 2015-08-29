package com.excelian.mache.core;

import com.google.common.cache.CacheLoader;

/**
 * Provides a base class that extends {@link CacheLoader} while implementing {@link MacheLoader}
 *
 * @param <K> the cache key type.
 * @param <V> the cache value type.
 * @param <D> the underlying data store session/connection type.
 */
public abstract class AbstractCacheLoader<K, V, D> extends CacheLoader<K, V> implements MacheLoader<K, V, D> {

}
