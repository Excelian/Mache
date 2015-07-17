package org.mache;

/**
 * Created by jbowkett on 17/07/15.
 */
public class PuttingCacheClient {

  public static void main(String[] args) throws Exception {
    final ExCache<String, Message> cache = new CassandraExample().exampleCache();
    System.out.println("Putting....");
    for (int i = 0; i < 50 ; i++) {
      final Message v = new Message("msg_"+i, "Hello World - "+i);
      cache.put(v.getPrimaryKey(), v);
    }
    System.out.println("Put complete.");
  }
}
