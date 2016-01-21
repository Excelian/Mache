package com.excelian.mache.couchbase.builder;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.builder.storage.AbstractConnectionContext;
import com.excelian.mache.core.AbstractCacheLoader;

import java.util.List;

/**
 * Created by jbowkett on 21/01/2016.
 */
public class CouchbaseConnectionContext extends AbstractConnectionContext<Cluster> {

    private final CouchbaseEnvironment builder;
    private final List<String> nodes;
    private Cluster cluster;

    private CouchbaseConnectionContext(CouchbaseEnvironment builder, List<String> nodes) {
        this.nodes = nodes;
        this.builder = builder;
    }

    @Override
    public Cluster getConnection(AbstractCacheLoader cacheLoader) {
        super.registerLoader(cacheLoader);
        if (cluster == null) {
            synchronized (this) {
                if (cluster == null) {
                    cluster = CouchbaseCluster.create(builder, nodes);
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
                    cluster.disconnect();
                    cluster = null;
                }
            }
        }
    }

    private static CouchbaseConnectionContext singletonInstance;

    public static CouchbaseConnectionContext getInstance(CouchbaseEnvironment couchbaseEnvironment, List<String> nodes) {
        if (singletonInstance == null) {
            synchronized (CouchbaseConnectionContext.class) {
                if (singletonInstance == null) {
                    singletonInstance = new CouchbaseConnectionContext(couchbaseEnvironment, nodes);
                }
            }
        }
        return singletonInstance;
    }
}
