package com.excelian.mache.examples;

/**
 * Provided arguments helper class.
 */
public class Args {
    final int count;
    final CacheType cacheType;
    final String host;

    public Args(int count, CacheType cacheType, String host) {
        this.count = count;
        this.cacheType = cacheType;
        this.host = host;
    }
}