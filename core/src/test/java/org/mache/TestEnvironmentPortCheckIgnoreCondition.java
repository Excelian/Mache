package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule;

public abstract class TestEnvironmentPortCheckIgnoreCondition implements ConditionalIgnoreRule.IgnoreCondition
{
    public TestEnvironmentPortCheckIgnoreCondition(int port)
    {
        if(new NotRunningInExcelian().isSatisfied()) {
            if (PortCheck.PortAccessible("localhost", port)) {
                hostName = "localhost";
                hostPresent = true;
            }
        }
        else {
            hostName = "10.28.1.140";// internal test environment - we don't check port as we'd like to fail if service is down
            hostPresent = true;
        }
    }

    private static String hostName="not-set";
    private boolean hostPresent =false;

    public static String HostName(){
        return hostName;
    }
    public boolean isSatisfied(){
        return !hostPresent;
    }
}
