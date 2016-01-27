package com.excelian.mache.mongo.builder;

import com.excelian.mache.builder.storage.AbstractConnectionContext;
import com.excelian.mache.core.MacheLoader;
import com.mongodb.ServerAddress;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Stores singletons that should be shared between Mongo connections.
 */
public class MongoConnectionContext extends AbstractConnectionContext<List<ServerAddress>> {

    private final ServerAddress[] seeds;

    /**
     * Constructor.
     * @param seeds the servers to connect to
     */
    public MongoConnectionContext(ServerAddress... seeds) {
        this.seeds = seeds;
    }

    @Override
    public List<ServerAddress> getConnection(MacheLoader cacheLoader) {
        super.registerLoader(cacheLoader);
        return stream(seeds).collect(toList());
    }

    @Override
    public void close() {
        return;
    }


    private static volatile MongoConnectionContext singleton;

    /**
     * Gets the singleton instance.
     * @param seeds the servers to connect to
     * @return the singleton instance
     */
    static MongoConnectionContext getInstance(ServerAddress... seeds) {
        if (singleton == null) {
            synchronized (MongoConnectionContext.class) {
                if (singleton == null) {
                    singleton = new MongoConnectionContext(seeds);
                }
            }
        }
        return singleton;
    }
}
