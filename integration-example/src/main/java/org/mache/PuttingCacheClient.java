package org.mache;

import org.mache.examples.mongo.MongoAnnotatedMessage;
import org.mache.examples.mongo.MongoExample;

/**
 * Created by jbowkett on 17/07/15.
 */
public class PuttingCacheClient {

  public static void main(String[] args) throws Exception {
    final ExCache<String, MongoAnnotatedMessage> cache = new MongoExample().exampleCache();
//    final ExCache<String, Message> cache = new CassandraExample().exampleCache();
    System.out.println("Putting....");
    for (int i = 0; i < 50 ; i++) {
      final MongoAnnotatedMessage v = new MongoAnnotatedMessage("msg_"+i, "Hello World - "+i);
      cache.put(v.getPrimaryKey(), v);
    }
    System.out.println("Put complete.");
  }
}
