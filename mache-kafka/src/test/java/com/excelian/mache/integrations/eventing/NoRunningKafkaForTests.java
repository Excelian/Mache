package com.excelian.mache.integrations.eventing;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningKafkaForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningKafkaForTests() {
        super(9092, "10.28.1.140");
    }
}
