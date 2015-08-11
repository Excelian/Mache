package org.mache;

public class MongoDbForTestsPresent extends TestEnvironmentPortCheckIgnoreCondition
{
    public MongoDbForTestsPresent()
    {
        super(27017);
    }
}



