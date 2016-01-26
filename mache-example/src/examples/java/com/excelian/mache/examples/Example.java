package com.excelian.mache.examples;

import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;

/**
 * Provides a {@link Mache} type for an example client.
 *
 * @param <T> The Spring Data Annotated object type.
 * @param <M> The value type to store
 */
public interface Example<T, M extends Example.KeyedMessge> {

    /**
     * Provides a {@link Mache} type for an example client.
     *
     * @return The example mache client.
     */
    Mache<String, T> exampleCache() throws Exception;

    M createEntity(String primaryKey, String msg);

    /**
     * A value type with a key.
     */
    public interface KeyedMessge {
        public String getPrimaryKey();
    }
}


