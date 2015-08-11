package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule;

public class MongoDbForTestsPresent implements ConditionalIgnoreRule.IgnoreCondition {
  static{

      if(PortCheck.PortAccessible("localhost", 27017))
      {
        hostName="localhost";
        FOUND_IT=true;
      }
      else  if(PortCheck.PortAccessible("10.28.1.140", 27017))
      {
          hostName="10.28.1.140";
          FOUND_IT=true;
      }
  }

  private static String hostName;
  private static boolean FOUND_IT=false;

  public static String HostName(){
    return hostName;
  }
  public boolean isSatisfied(){
    return FOUND_IT;
  }

}
