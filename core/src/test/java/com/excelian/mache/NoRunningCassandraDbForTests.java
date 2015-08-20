package com.excelian.mache;

public class NoRunningCassandraDbForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningCassandraDbForTests() {
        super(9042, "nowhere");
    }
}


