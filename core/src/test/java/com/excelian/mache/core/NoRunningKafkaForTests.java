package com.excelian.mache.core;

public class NoRunningKafkaForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningKafkaForTests() {
        super(9092, "10.28.1.140");
    }
}
