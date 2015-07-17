package org.mache;

import org.mache.examples.mongo.MessageMongoAnnotated;
import org.mache.examples.mongo.MongoExample;

/**
 * Created by jbowkett on 17/07/15.
 */
public class GettingCacheClient {

  public static void main(String[] args) throws Exception {
//    final ExCache<String, Message> cache = new CassandraExample().exampleCache();
    final ExCache<String, MessageMongoAnnotated> cache = new MongoExample().exampleCache();
    System.out.println("Getting...");
    for (int i = 0; i < 50 ; i++) {
      final MessageMongoAnnotated hello = cache.get("msg_"+i);
      System.out.println("hello = " + hello);
    }
    cache.close();
  }
}
