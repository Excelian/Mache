package org.mache;

import org.mache.examples.cassandra.CassandraAnnotatedMessage;
import org.mache.examples.cassandra.CassandraExample;
import org.mache.examples.mongo.MongoAnnotatedMessage;
import org.mache.examples.mongo.MongoExample;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * Created by jbowkett on 17/07/15.
 */
public class GettingCacheClient {

  private enum CacheType {Cassandra, Mongo}

  public static void main(String...commandLine) {
    final Args args = parseArgs(commandLine);
    final int count = args.count;
    switch(args.cacheType){
      case Cassandra:
        doCassandra(count);
        break;
      case Mongo:
        doMongo(count);
        break;
    }
  }

  private static void doMongo(int count) {
    try (final MongoExample mongoExample = new MongoExample()) {
      doMongoExample(count, mongoExample);
    }
    catch (IOException | JMSException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private static void doCassandra(int count) {
    try (final CassandraExample mongoExample = new CassandraExample()) {
      doCassandraExample(count, mongoExample);
    }
    catch (JMSException | IOException | ExecutionException e) {
      e.printStackTrace();
    }
  }

  private static void doMongoExample(int count, MongoExample mongoExample) throws IOException, JMSException, ExecutionException {
    final ExCache<String, MongoAnnotatedMessage> cache = mongoExample.exampleCache();
    System.out.println("Getting...");
    for (int i = 0; i < count ; i++) {
      final MongoAnnotatedMessage hello = cache.get("msg_"+i);
      System.out.println("hello = " + hello);
    }
  }

  private static void doCassandraExample(int count, CassandraExample example) throws IOException, JMSException, ExecutionException {
    final ExCache<String, CassandraAnnotatedMessage> cache = example.exampleCache();
    System.out.println("Getting...");
    for (int i = 0; i < count ; i++) {
      final CassandraAnnotatedMessage hello = cache.get("msg_"+i);
      System.out.println("hello = " + hello);
    }
  }

  private static Args parseArgs(String[] args) {
    if (args.length == 2){
      final CacheType cacheType = CacheType.valueOf(args[1]);
      final int count = Integer.parseInt(args[0]);
      return new Args(count, cacheType);
    }
    else{
      throw new RuntimeException("Usage : GettingCacheClient <get count> "+ Arrays.toString(CacheType.values()));
    }
  }

  private static class Args {
    private final int count;
    private final CacheType cacheType;
    public Args(int count, CacheType cacheType) {
      this.count = count;
      this.cacheType = cacheType;
    }
  }
}
