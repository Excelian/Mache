package org.mache;

public class CassandraDbForTestsPresent extends TestEnvironmentPortCheckIgnoreCondition
{
    public CassandraDbForTestsPresent()
    {
        super(9042);
    }
}
