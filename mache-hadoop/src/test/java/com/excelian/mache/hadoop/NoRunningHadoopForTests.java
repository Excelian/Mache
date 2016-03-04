package com.excelian.mache.hadoop;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningHadoopForTests extends TestEnvironmentPortCheckIgnoreCondition {

    public static final String HOST = "192.168.99.100";
    public static final int PORT = 9000;

    public NoRunningHadoopForTests() {
        // Check the external port as the ip is the default docker ip on windows
        // and the container may be running something else.
        super(PORT, HOST, true);
    }
}


