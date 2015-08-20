package com.excelian.mache.examples;

import com.excelian.mache.core.ExCache;

/**
 * Created by jbowkett on 14/08/15.
 */
public interface Example<T> {
  ExCache<String, T> exampleCache();
}
