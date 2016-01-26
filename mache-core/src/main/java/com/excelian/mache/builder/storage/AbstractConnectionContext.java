package com.excelian.mache.builder.storage;

import com.excelian.mache.core.MacheLoader;

import java.util.HashSet;
import java.util.Set;

/**
 * Builds a Mache instance
 *
 * @param <T> The type of the shared resource, often a cluster, depending on
 *            the platform.
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
