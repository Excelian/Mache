package com.excelian.mache.builder.storage;

import com.excelian.mache.core.AbstractCacheLoader;

public interface ConnectionContext<C> extends AutoCloseable {
    C getConnection(AbstractCacheLoader cacheLoader);

    void close(AbstractCacheLoader cacheLoader);

    void close();
}
