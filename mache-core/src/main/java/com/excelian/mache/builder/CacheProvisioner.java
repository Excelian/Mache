package com.excelian.mache.builder;

import com.excelian.mache.core.Cache;

/**
 * Created by jbowkett on 11/01/2016.
 */
public interface CacheProvisioner {
    <K, V> Cache<K, V> getCache();
}
