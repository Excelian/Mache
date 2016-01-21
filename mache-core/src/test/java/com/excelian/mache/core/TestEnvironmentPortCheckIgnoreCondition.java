package com.excelian.mache.core;

import com.codeaffine.test.ConditionalIgnoreRule;

import java.io.IOException;
import java.net.InetAddress;

import static com.excelian.mache.core.PortCheck.portAccessible;

public abstract class TestEnvironmentPortCheckIgnoreCondition implements ConditionalIgnoreRule.IgnoreCondition {
    protected final String host;
    private final boolean hostPresent;

    public TestEnvironmentPortCheckIgnoreCondition(int port, String testEnvIp) {
        if (hostIsReachable(testEnvIp)) {
            host = testEnvIp;// internal test environment - we don't check port as we'd like to fail if service is down
            hostPresent = true;
        } else if (portAccessible("localhost", port)) {
            host = "localhost";
            hostPresent = true;
        } else {
            host = "not-set-as-no-host-running";
            hostPresent = false;
        }
    }

    public static boolean hostIsReachable(String host) {
        try {
            return InetAddress.getByName(host).isReachable(2000);
        } catch (IOException e) {
            return false;
        }
    }

    public String getHost() {
        return host;
    }

    public boolean isSatisfied() {
        return !hostPresent;
    }
}
