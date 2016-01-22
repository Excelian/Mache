package com.excelian.mache.builder.storage;

import com.excelian.mache.core.MacheLoader;

public interface ConnectionContext<C> extends AutoCloseable {
    C getConnection(MacheLoader cacheLoader);

    void close(MacheLoader cacheLoader);

    void close();
}
