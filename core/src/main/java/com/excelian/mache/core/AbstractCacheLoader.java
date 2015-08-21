package com.excelian.mache.core;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;

/**
 * NB: Unfortunately CacheLoader is not an interface.
 */
public abstract class AbstractCacheLoader<K, V, D> extends CacheLoader<K, V> implements MacheLoader<K, V, D> {

    public void onRemoval(RemovalNotification<K, V> notification) {
        if (notification.getCause().equals(RemovalCause.EXPLICIT)) {
            this.remove(notification.getKey());
        }
    }
}
