package com.excelian.mache.cassandra.builder;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.builder.storage.AbstractConnectionContext;
import com.excelian.mache.core.MacheLoader;

/**
 * Created by jbowkett on 20/01/2016.
 */
public class CassandraConnectionContext extends AbstractConnectionContext<Cluster> {

    private final Cluster.Builder builder;
    private Cluster cluster;

    private CassandraConnectionContext(Cluster.Builder builder) {
        this.builder = builder;
    }

    @Override
    public Cluster getConnection(MacheLoader cacheLoader) {
        registerLoader(cacheLoader);
        if (cluster == null) {
            synchronized (this) {
                if (cluster == null) {
                    cluster = builder.build();
                }
            }
        }
        return cluster;
    }

    @Override
    public void close() {
        if (cluster != null) {
            synchronized (this) {
                if (cluster != null) {
                    cluster.close();
                    cluster = null;
                }
            }
        }
    }

    private static CassandraConnectionContext singleton;

    static CassandraConnectionContext getInstance(Cluster.Builder clusterBuilder) {
        if (singleton == null) {
            synchronized (CassandraConnectionContext.class) {
                if (singleton == null) {
                    singleton = new CassandraConnectionContext(clusterBuilder);
                }
            }
        }
        return singleton;
    }
}
