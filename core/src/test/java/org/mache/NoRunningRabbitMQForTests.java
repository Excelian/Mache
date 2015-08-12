package org.mache;

public class NoRunningRabbitMQForTests extends TestEnvironmentPortCheckIgnoreCondition
{
    public NoRunningRabbitMQForTests()
    {
        super(5672);
    }
}
