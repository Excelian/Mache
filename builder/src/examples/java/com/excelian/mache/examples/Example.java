package com.excelian.mache.examples;

import com.excelian.mache.core.Mache;

/**
 * Created by jbowkett on 14/08/15.
 */
public interface Example<T> {
    Mache<String, T> exampleCache();
}
