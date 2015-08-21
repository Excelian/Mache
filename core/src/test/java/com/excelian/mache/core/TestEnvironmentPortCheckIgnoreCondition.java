package com.excelian.mache.core;

import com.codeaffine.test.ConditionalIgnoreRule;

import static com.excelian.mache.core.PortCheck.PortAccessible;

public abstract class TestEnvironmentPortCheckIgnoreCondition implements ConditionalIgnoreRule.IgnoreCondition {
    protected final String hostName;
    private final boolean hostPresent;

    public TestEnvironmentPortCheckIgnoreCondition(int port, String testEnvIp) {
        if (NotRunningInExcelian.hostIsReachable(testEnvIp)) {
            hostName = testEnvIp;// internal test environment - we don't check port as we'd like to fail if service is down
            hostPresent = true;
        }
        else if (PortAccessible("localhost", port)) {
            hostName = "localhost";
            hostPresent = true;
        }
        else{
            hostName = "not-set";
            hostPresent = false;
        }
    }

    public String HostName() {
        return hostName;
    }

    public boolean isSatisfied() {
        return !hostPresent;
    }
}
