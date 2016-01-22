package com.excelian.mache.builder.storage;

import com.excelian.mache.core.MacheLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by jbowkett on 20/01/2016.
 */
public abstract class AbstractConnectionContext<T> implements ConnectionContext<T> {
    private final Set<MacheLoader> loadersUsingCluster = new HashSet<>();

    protected void registerLoader(MacheLoader cacheLoader) {
        loadersUsingCluster.add(cacheLoader);
    }


    @Override
    public void close(MacheLoader cacheLoader) {
        loadersUsingCluster.remove(cacheLoader);
        if (loadersUsingCluster.size() == 0) {
            close();
        }
    }
}
