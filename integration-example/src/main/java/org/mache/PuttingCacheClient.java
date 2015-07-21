package org.mache;

import org.mache.examples.mongo.MongoAnnotatedMessage;
import org.mache.examples.mongo.MongoExample;

import javax.jms.JMSException;
import java.io.IOException;

/**
 * Created by jbowkett on 17/07/15.
 */
public class PuttingCacheClient {

  public static void main(String...args)  {
    final int count = extractCountFrom(args);
    try(final MongoExample mongoEg = new MongoExample()) {
      final ExCache<String, MongoAnnotatedMessage> cache = mongoEg.exampleCache();
      //    final ExCache<String, Message> cache = new CassandraExample().exampleCache();
      System.out.println("Putting....");
      for (int i = 0; i < count; i++) {
        final MongoAnnotatedMessage v = new MongoAnnotatedMessage("msg_" + i, "Hello World - " + i);
        cache.put(v.getPrimaryKey(), v);
      }
      System.out.println("Put complete.");
    }
    catch (JMSException | IOException e) {
      e.printStackTrace();
    }
  }

  private static int extractCountFrom(String[] args) {
    return args.length == 1 ? Integer.parseInt(args[0]) : 100;
  }
}
