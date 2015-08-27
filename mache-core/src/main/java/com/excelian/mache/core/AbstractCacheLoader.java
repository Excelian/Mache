package com.excelian.mache.core;

import com.google.common.cache.CacheLoader;

/**
 * NB: Unfortunately CacheLoader is not an interface.
 */
public abstract class AbstractCacheLoader<K, V, D> extends CacheLoader<K, V> implements MacheLoader<K, V, D> {

}
