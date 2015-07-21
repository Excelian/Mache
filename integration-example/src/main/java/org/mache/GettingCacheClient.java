package org.mache;

import org.mache.examples.mongo.MongoAnnotatedMessage;
import org.mache.examples.mongo.MongoExample;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by jbowkett on 17/07/15.
 */
public class GettingCacheClient {

  public static void main(String...args) {
    final int count = extractCountFrom(args);
//    final ExCache<String, Message> cache = new CassandraExample().exampleCache();
    final ExCache<String, MongoAnnotatedMessage> cache;
    try (final MongoExample mongoExample = new MongoExample()) {
      cache = mongoExample.exampleCache();
      System.out.println("Getting...");
      for (int i = 0; i < count ; i++) {
        final MongoAnnotatedMessage hello = cache.get("msg_"+i);
        System.out.println("hello = " + hello);
      }
    }
    catch (IOException | JMSException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private static int extractCountFrom(String[] args) {
    return args.length == 1 ? Integer.parseInt(args[0]) : 100;
  }
}
