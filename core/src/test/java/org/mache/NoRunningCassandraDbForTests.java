package org.mache;

public class NoRunningCassandraDbForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningCassandraDbForTests() {
        super(9042);
    }
}


