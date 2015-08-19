package org.mache;

public class NoRunningMongoDbForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningMongoDbForTests() {
        super(27017);
    }
}



