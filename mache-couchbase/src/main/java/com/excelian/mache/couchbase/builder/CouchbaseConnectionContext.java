package com.excelian.mache.couchbase.builder;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.builder.storage.AbstractConnectionContext;
import com.excelian.mache.core.MacheLoader;

import java.util.List;

/**
 * Manages access to couchbase cluster.
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
    public Cluster getConnection(MacheLoader cacheLoader) {
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

    /**
     * Gets the singleton.
     * @param couchbaseEnvironment - the single couchbase env, if null then the
     *                             default is instantiated
     * @param nodes - the nodes to connect to
     * @return the singleton connection context
     */
    public static CouchbaseConnectionContext getInstance(CouchbaseEnvironment couchbaseEnvironment,
                                                         List<String> nodes) {
        if (singletonInstance == null) {
            synchronized (CouchbaseConnectionContext.class) {
                if (singletonInstance == null) {
                    if (couchbaseEnvironment == null) {
                        couchbaseEnvironment = DefaultCouchbaseEnvironment.create();
                    }
                    singletonInstance = new CouchbaseConnectionContext(couchbaseEnvironment, nodes);
                }
            }
        }
        return singletonInstance;
    }
}
