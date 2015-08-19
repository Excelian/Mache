package org.mache;

public class NoRunningKafkaForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningKafkaForTests() {
        super(9092);
    }
}
