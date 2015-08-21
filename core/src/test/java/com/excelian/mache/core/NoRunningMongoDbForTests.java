package com.excelian.mache.core;

public class NoRunningMongoDbForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningMongoDbForTests() {
        super(27017, "10.28.1.140");
    }
}



