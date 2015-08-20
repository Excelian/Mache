package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule.IgnoreCondition;

import java.io.IOException;
import java.net.InetAddress;

public class NotRunningInExcelian implements IgnoreCondition {

    public static boolean hostIsReachable(String host){
        try {
            return InetAddress.getByName(host).isReachable(2000);
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean RUNNING_IN_EXCELIAN = hostIsReachable("10.28.1.140");

    public boolean isSatisfied() {
        return !RUNNING_IN_EXCELIAN;
    }

}

