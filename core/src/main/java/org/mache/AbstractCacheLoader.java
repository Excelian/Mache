package org.mache;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;

/**
 * Created by neil.avery on 27/05/2015.
 *
 * NB: Unfortunately CacheLoader is not an interface.
 */
abstract public class AbstractCacheLoader<K,V,D> extends CacheLoader<K,V> implements ExCacheLoader<K,V,D> {

    public void onRemoval(RemovalNotification<K, V> notification) {
        if (notification.getCause().equals(RemovalCause.EXPLICIT)) {
            this.remove(notification.getKey());
        }
    }
}
