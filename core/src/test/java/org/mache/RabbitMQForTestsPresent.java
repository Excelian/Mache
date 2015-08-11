package org.mache;

public class RabbitMQForTestsPresent extends TestEnvironmentPortCheckIgnoreCondition
{
    public RabbitMQForTestsPresent()
    {
        super(5672);
    }
}
