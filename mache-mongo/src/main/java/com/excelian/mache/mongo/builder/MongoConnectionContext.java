package com.excelian.mache.mongo.builder;

import com.excelian.mache.builder.storage.AbstractConnectionContext;
import com.excelian.mache.core.MacheLoader;
import com.mongodb.ServerAddress;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import java.util.List;

/**
 * Created by jbowkett on 20/01/2016.
 */
public class MongoConnectionContext extends AbstractConnectionContext<List<ServerAddress>> {

    private final ServerAddress[] seeds;

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


    private static MongoConnectionContext singleton;

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
