package com.excelian.mache.hadoop;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningHadoopForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningHadoopForTests() {
        // Check the external port as the ip is the default docker ip on windows
        // and the container may be running something else.
        super(9000, "192.168.99.100", true);
    }
}


