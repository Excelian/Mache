package com.excelian.mache.core;

public class NoRunningRabbitMQForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningRabbitMQForTests() {
        super(5672, "10.28.1.140");
    }
}
