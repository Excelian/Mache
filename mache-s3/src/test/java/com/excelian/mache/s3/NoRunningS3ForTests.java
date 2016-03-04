package com.excelian.mache.s3;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningS3ForTests extends TestEnvironmentPortCheckIgnoreCondition {

    public static final String HOST = "localhost";
    public static final int PORT = 4567;

    public NoRunningS3ForTests() {
        super(PORT, HOST);
    }

    @Override
    public boolean hostIsReachable(String host) {
        // As we check on localhost only want to see if port open
        return false;
    }
}


