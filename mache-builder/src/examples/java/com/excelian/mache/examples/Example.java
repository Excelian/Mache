package com.excelian.mache.examples;

import com.excelian.mache.core.Mache;

/**
 * Provides a {@link Mache} type for an example client.
 *
 * @param <T> The Spring Data Annotated object type.
 */
public interface Example<T> {
    /**
     * Provides a {@link Mache} type for an example client.
     *
     * @return The example mache client.
     */
    Mache<String, T> exampleCache();
}
