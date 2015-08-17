package org.mache.examples;

import org.mache.ExCache;

/**
 * Created by jbowkett on 14/08/15.
 */
public interface Example<T> {
  ExCache<String, T> exampleCache();
}
