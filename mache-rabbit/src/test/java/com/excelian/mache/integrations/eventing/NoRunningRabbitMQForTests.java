package com.excelian.mache.integrations.eventing;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningRabbitMQForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningRabbitMQForTests() {
        super(5672, "10.28.1.140");
    }
}
