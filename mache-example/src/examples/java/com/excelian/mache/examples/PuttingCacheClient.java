package com.excelian.mache.examples;

import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.examples.cassandra.CassandraAnnotatedMessage;
import com.excelian.mache.examples.cassandra.CassandraExample;
import com.excelian.mache.examples.couchbase.CouchbaseAnnotatedMessage;
import com.excelian.mache.examples.couchbase.CouchbaseExample;
import com.excelian.mache.examples.mongo.MongoAnnotatedMessage;
import com.excelian.mache.examples.mongo.MongoExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Shows a simple example of putting objects into the different {@link com.excelian.mache.core.MacheLoader}
 * implementations.
 */
public class PuttingCacheClient {
    private static final Logger LOG = LoggerFactory.getLogger(PuttingCacheClient.class);

    public static void main(String... commandLine) throws Exception {
        final Args args = parseArgs(commandLine);
        final int count = args.count;
        Example example;

        switch (args.cacheType) {
            case Cassandra:
                example = new CassandraExample("192.168.2.9");
                break;
            case Mongo:
                example =  new MongoExample("10.28.1.140");
                break;
            case Couchbase:
                example = new CouchbaseExample("10.28.1.140");
                break;

            default:
                throw new RuntimeException("Invalid cache type: [" + args.cacheType + "].  Valid values are:"
                        + Arrays.toString(CacheType.values()));
        }

        populateWithMsgs(count, example);
    }

    private static void populateWithMsgs(int count, Example example) throws Exception {

        try(ConnectionContext context =  example.createConnectionContext()) {
            try(Mache mache = example.exampleCache(context)) {
                LOG.info("Putting...");
                for (int i = 0; i < count; i++) {
                    Example.KeyedMessge v = example.createEntity("msg_" + i, "Hello World - " + i);

                    mache.put(v.getPrimaryKey(), v);
                }
            }
        }
    }


    private static Args parseArgs(String[] args) {
        if (args.length == 2) {
            final CacheType cacheType = CacheType.valueOf(args[1]);
            final int count = Integer.parseInt(args[0]);
            return new Args(count, cacheType);
        } else {
            throw new RuntimeException("Usage : PuttingCacheClient <put count> " + Arrays.toString(CacheType.values()));
        }
    }
}
