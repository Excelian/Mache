package com.excelian.mache.examples;

import com.excelian.mache.core.Mache;
import com.excelian.mache.examples.cassandra.CassandraAnnotatedMessage;
import com.excelian.mache.examples.cassandra.CassandraExample;
import com.excelian.mache.examples.mongo.MongoAnnotatedMessage;
import com.excelian.mache.examples.mongo.MongoExample;

import java.util.Arrays;

/**
 * Created by jbowkett on 17/07/15.
 */
public class PuttingCacheClient {
    public static void main(String... commandLine) {
        final Args args = parseArgs(commandLine);
        final int count = args.count;
        switch (args.cacheType) {
            case Cassandra:
                populateWithCassandraMsgs(count, new CassandraExample().exampleCache());
                break;
            case Mongo:
                populateWithMongoMsgs(count, new MongoExample().exampleCache());
                break;
            default:
                throw new RuntimeException("Invalid cache type: [" + args.cacheType + "].  Valid values are:" + Arrays.toString(CacheType.values()));
        }
    }

    private static void populateWithMongoMsgs(int count, Mache<String, MongoAnnotatedMessage> cache) {
        System.out.println("Putting...");
        for (int i = 0; i < count; i++) {
            final MongoAnnotatedMessage v = new MongoAnnotatedMessage("msg_" + i, "Hello World - " + i);
            cache.put(v.getPrimaryKey(), v);
        }
    }

    private static void populateWithCassandraMsgs(int count, Mache<String, CassandraAnnotatedMessage> cache) {
        System.out.println("Putting...");
        for (int i = 0; i < count; i++) {
            final CassandraAnnotatedMessage v = new CassandraAnnotatedMessage("msg_" + i, "Hello World - " + i);
            cache.put(v.getPrimaryKey(), v);
        }
    }

    private static Args parseArgs(String[] args) {
        if (args.length == 2) {
            final CacheType cacheType = CacheType.valueOf(args[1]);
            final int count = Integer.parseInt(args[0]);
            return new Args(count, cacheType);
        }
        else {
            throw new RuntimeException("Usage : PuttingCacheClient <put count> " + Arrays.toString(CacheType.values()));
        }
    }

    private enum CacheType {Cassandra, Mongo}

    private static class Args {
        private final int count;
        private final CacheType cacheType;

        public Args(int count, CacheType cacheType) {
            this.count = count;
            this.cacheType = cacheType;
        }
    }
}
