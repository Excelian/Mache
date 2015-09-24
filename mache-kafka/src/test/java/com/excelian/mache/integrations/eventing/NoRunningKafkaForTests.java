package com.excelian.mache.integrations.eventing;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningKafkaForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningKafkaForTests() {
        super(9092, "192.168.3.4");
    }
}
