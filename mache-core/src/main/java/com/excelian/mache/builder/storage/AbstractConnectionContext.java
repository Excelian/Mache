package com.excelian.mache.builder.storage;

import com.excelian.mache.core.AbstractCacheLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jbowkett on 20/01/2016.
 */
public abstract class AbstractConnectionContext<T> implements ConnectionContext<T> {
    private final Set<AbstractCacheLoader> loadersUsingCluster = new HashSet<>();

    protected void registerLoader(AbstractCacheLoader cacheLoader) {
        loadersUsingCluster.add(cacheLoader);
    }


    @Override
    public void close(AbstractCacheLoader cacheLoader) {
        loadersUsingCluster.remove(cacheLoader);
        if (loadersUsingCluster.size() == 0) {
            close();
        }
    }
}
