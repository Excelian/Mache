package com.excelian.mache.core;

import com.codeaffine.test.ConditionalIgnoreRule;

import static com.excelian.mache.core.PortCheck.PortAccessible;

public abstract class TestEnvironmentPortCheckIgnoreCondition implements ConditionalIgnoreRule.IgnoreCondition {
    protected final String host;
    private final boolean hostPresent;

    public TestEnvironmentPortCheckIgnoreCondition(int port, String testEnvIp) {
        if (NotRunningInExcelian.hostIsReachable(testEnvIp)) {
            host = testEnvIp;// internal test environment - we don't check port as we'd like to fail if service is down
            hostPresent = true;
        } else if (PortAccessible("localhost", port)) {
            host = "localhost";
            hostPresent = true;
        } else {
            host = "not-set";
            hostPresent = false;
        }
    }

    public String getHost() {
        return host;
    }

    public boolean isSatisfied() {
        return !hostPresent;
    }
}
