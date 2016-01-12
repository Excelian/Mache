package com.excelian.mache.guava.builder;

import com.excelian.mache.builder.CacheProvisioner;
import com.excelian.mache.core.Cache;
import com.excelian.mache.guava.GuavaCache;

/**
 * Created by jbowkett on 11/01/2016.
 */
public class GuavaProvisioner implements CacheProvisioner {

    public static CacheProvisioner guava() {
        return new GuavaProvisioner();
    }

    @Override
    public <K, V> Cache<K, V> getCache() {
        return new GuavaCache<>();
    }
}
