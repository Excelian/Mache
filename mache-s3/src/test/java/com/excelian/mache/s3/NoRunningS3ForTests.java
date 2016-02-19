package com.excelian.mache.s3;

import com.excelian.mache.core.TestEnvironmentPortCheckIgnoreCondition;

public class NoRunningS3ForTests extends TestEnvironmentPortCheckIgnoreCondition {
    public NoRunningS3ForTests() {
        super(4567, "localhost");
    }

    @Override
    public boolean hostIsReachable(String host) {
        // As we check on localhost only want to see if port open
        return false;
    }
}


