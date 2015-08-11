package org.mache;

public class KafkaForTestsPresent extends TestEnvironmentPortCheckIgnoreCondition
{
    public KafkaForTestsPresent()
    {
        super(9092);
    }
}
