package com.excelian.mache.hadoop;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningHadoopForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningHadoopForTests() {
        super(9000, "192.168.99.100");
    }
}


