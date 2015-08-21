package com.excelian.mache.core;

public class NoRunningCassandraDbForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningCassandraDbForTests() {
        super(9042, "nowhere");
    }
}


