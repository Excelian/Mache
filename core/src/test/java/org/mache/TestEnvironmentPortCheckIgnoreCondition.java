package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule;

public abstract class TestEnvironmentPortCheckIgnoreCondition implements ConditionalIgnoreRule.IgnoreCondition
{
    public TestEnvironmentPortCheckIgnoreCondition(int port)
    {
        if(new NotRunningInExcelian().isSatisfied()) {
            if (PortCheck.PortAccessible("localhost", port)) {
                hostName = "localhost";
                FOUND_IT = true;
            }
        }
        else {
            hostName = "10.28.1.140";// internal test environment - we don't check port as we'd like to fail if service is down
            FOUND_IT = true;
        }
    }

    private static String hostName="not-set";
    private boolean FOUND_IT=false;

    public static String HostName(){
        return hostName;
    }
    public boolean isSatisfied(){
        return !FOUND_IT;
    }
}
