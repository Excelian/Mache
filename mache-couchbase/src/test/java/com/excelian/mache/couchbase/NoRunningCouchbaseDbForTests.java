package com.excelian.mache.couchbase;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningCouchbaseDbForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningCouchbaseDbForTests() {
        super(8091, "192.168.56.100");
    }
}


