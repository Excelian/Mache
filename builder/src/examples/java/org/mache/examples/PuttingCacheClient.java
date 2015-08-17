package org.mache.examples;

import org.mache.ExCache;
import org.mache.examples.cassandra.CassandraAnnotatedMessage;
import org.mache.examples.cassandra.CassandraExample;
import org.mache.examples.mongo.MongoAnnotatedMessage;
import org.mache.examples.mongo.MongoExample;

import java.util.Arrays;

/**
 * Created by jbowkett on 17/07/15.
 */
public class PuttingCacheClient {
  private enum CacheType {Cassandra, Mongo}

  public static void main(String...commandLine) {
    final Args args = parseArgs(commandLine);
    final int count = args.count;
    switch(args.cacheType){
      case Cassandra:
        populateWithCassandraMsgs(count, new CassandraExample().exampleCache());
        break;
      case Mongo:
        populateWithMongoMsgs(count, new MongoExample().exampleCache());
        break;
      default:
        throw new RuntimeException("Invalid cache type: ["+args.cacheType+"].  Valid values are:"+Arrays.toString(CacheType.values()));
    }
  }

  private static void populateWithMongoMsgs(int count, ExCache<String, MongoAnnotatedMessage> cache) {
    System.out.println("Putting...");
    for(int i = 0; i< count ; i++){
      final MongoAnnotatedMessage v = new MongoAnnotatedMessage("msg_" + i, "Hello World - " + i);
      cache.put(v.getPrimaryKey(), v);
    }
  }

  private static void populateWithCassandraMsgs(int count, ExCache<String, CassandraAnnotatedMessage> cache)  {
    System.out.println("Putting...");
    for (int i = 0; i < count ; i++) {
      final CassandraAnnotatedMessage v = new CassandraAnnotatedMessage("msg_" + i, "Hello World - " + i);
      cache.put(v.getPrimaryKey(), v);
    }
  }

  private static Args parseArgs(String[] args) {
    if (args.length == 2){
      final CacheType cacheType = CacheType.valueOf(args[1]);
      final int count = Integer.parseInt(args[0]);
      return new Args(count, cacheType);
    }
    else{
      throw new RuntimeException("Usage : PuttingCacheClient <put count> "+ Arrays.toString(CacheType.values()));
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