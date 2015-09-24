package com.excelian.mache.cassandra;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningCassandraDbForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningCassandraDbForTests() {
        super(9042, "nowhere");
    }
}


