package com.excelian.mache.core;

public class NoRunningCouchbaseDbForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningCouchbaseDbForTests() {
        super(8091, "10.28.1.140");
    }
}


