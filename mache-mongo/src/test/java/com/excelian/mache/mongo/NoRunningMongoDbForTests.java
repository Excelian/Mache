package com.excelian.mache.mongo;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningMongoDbForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningMongoDbForTests() {
        super(27017, "10.28.1.140");
    }
}



