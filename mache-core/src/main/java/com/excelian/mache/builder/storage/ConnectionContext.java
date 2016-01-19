package com.excelian.mache.builder.storage;

public interface ConnectionContext<C> extends AutoCloseable {
    C getStorage();
}
