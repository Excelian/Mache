package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule.IgnoreCondition;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by jbowkett on 22/07/15.
 */
public class NotRunningInExcelian implements IgnoreCondition {
  static{
    try {
      RUNNING_IN_EXCELIAN = InetAddress.getByName("10.28.1.140").isReachable(30);
    }
    catch (IOException e) {
      RUNNING_IN_EXCELIAN = false;
    }
  }
  private static boolean RUNNING_IN_EXCELIAN;

  public boolean isSatisfied(){
    return !RUNNING_IN_EXCELIAN;
  }

}
