package org.mache;

/**
 * Created by jbowkett on 17/07/15.
 */
public class GettingCacheClient {

  public static void main(String[] args) throws Exception {
    final ExCache<String, Message> cache = new CassandraExample().exampleCache();
    System.out.println("Getting...");
    for (int i = 0; i < 50 ; i++) {
      final Message hello = cache.get("msg_"+i);
      System.out.println("hello = " + hello);
    }
    cache.close();
  }
}
