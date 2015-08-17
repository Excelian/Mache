package org.mache.examples;

import org.mache.ExCache;
import org.mache.examples.cassandra.CassandraExample;
import org.mache.examples.mongo.MongoExample;

import java.util.Arrays;

/**
 * Created by jbowkett on 17/07/15.
 */
public class GettingCacheClient {

  private enum CacheType {Cassandra, Mongo}

  public static void main(String...commandLine) {
    final Args args = parseArgs(commandLine);
    final int count = args.count;
    final Example example;
    switch(args.cacheType){
      case Cassandra:
        example = new CassandraExample();
        break;
      case Mongo:
        example = new MongoExample();
        break;
      default:
        throw new RuntimeException("Invalid cache type: ["+args.cacheType+"].  Valid values are:"+Arrays.toString(CacheType.values()));
    }
    doExample(count, example);
  }

  private static <T> void doExample(int count, Example<T> example) {
    final ExCache<String, T> cache = example.exampleCache();
    System.out.println("Getting...");
    for (int i = 0; i < count ; i++) {
      final T hello = cache.get("msg_"+i);
      System.out.println("hello = " + hello);
    }
    cache.close();
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
