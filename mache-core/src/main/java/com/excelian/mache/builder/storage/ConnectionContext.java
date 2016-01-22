package com.excelian.mache.builder.storage;

import com.excelian.mache.core.MacheLoader;

/**
 * The context for connections to the datastore, used to manage shared
 * resources that are often singletons as specified by the driver.
 *
 * @param <T> The type of the shared resource, often a cluster, depending on
 *           the platform.
 */
public interface ConnectionContext<T> extends AutoCloseable {
    T getConnection(MacheLoader cacheLoader);

    void close(MacheLoader cacheLoader);

    void close();
}
