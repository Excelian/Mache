package com.excelian.mache.examples;

import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.examples.cassandra.CassandraExample;
import com.excelian.mache.examples.couchbase.CouchbaseExample;
import com.excelian.mache.examples.mongo.MongoExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Shows a simple example of getting objects from the different {@link com.excelian.mache.core.MacheLoader}
 * implementations.
 */
public class GettingCacheClient {

    private static final Logger LOG = LoggerFactory.getLogger(GettingCacheClient.class);

    public static void main(String... commandLine) throws Exception {
        final Args args = parseArgs(commandLine);
        final int count = args.count;
        final String hostAddress = args.host;
        final Example example;
        switch (args.cacheType) {
            case Cassandra:
                example = new CassandraExample(hostAddress);
                break;
            case Mongo:
                example = new MongoExample(hostAddress);
                break;
            case Couchbase:
                example = new CouchbaseExample(hostAddress);
                break;
            default:
                throw new RuntimeException("Invalid cache type: [" + args.cacheType + "].  Valid values are:"
                        + Arrays.toString(CacheType.values()));
        }
        doExample(count, example);
    }

    private static <T,C, M extends Example.KeyedMessge> void doExample(int count, Example<T,C, M> example) throws Exception {

        try(ConnectionContext<C> context = example.createConnectionContext()) {
            try(final Mache<String, T> cache = example.exampleCache(context)) {
                LOG.info("Getting...");
                for (int i = 0; i < count; i++) {
                    final T hello = cache.get("msg_" + i);
                    LOG.info("hello = " + hello);
                }
            }
        }
    }

    private static Args parseArgs(String[] args) {
        if (args.length == 3) {
            final int count = Integer.parseInt(args[0]);
            final CacheType cacheType = CacheType.valueOf(args[1]);
            final String hostAddress = args[2];

            return new Args(count, cacheType, hostAddress);
        } else {
            throw new RuntimeException("Usage : GettingCacheClient <get count> " + Arrays.toString(CacheType.values()));
        }
    }
}
