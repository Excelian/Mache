package com.excelian.mache.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.cluster.DefaultBucketSettings;
import com.excelian.mache.core.AbstractCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import java.util.concurrent.TimeUnit;

public class CouchbaseCacheLoader<K extends String, V> extends AbstractCacheLoader<K, V, Cluster> {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseCacheLoader.class);
    public static final long TIMEOUT = 20;

    private Cluster cluster;
    private ClusterManager manager;
    private final CouchbaseConfig config;
    private CouchbaseTemplate template;

    public CouchbaseCacheLoader(CouchbaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Couchbase config must be provided.");
        }
        this.config = config;
    }

    @Override
    public void create() {
        if (cluster == null) {
            synchronized (this) {
                if (cluster == null) {
                    LOG.info("Attempting to connect to Couchbase cluster hosted at {}", config.getServerAddresses());

                    cluster = CouchbaseCluster.create(config.getCouchbaseEnvironment(), config.getServerAddresses());
                    manager = cluster.clusterManager(config.getAdminUser(), config.getAdminPassword());

                    dropBucket();

                    createBucket();

                    Bucket bucket = cluster.openBucket(config.getBucketName(), TIMEOUT, TimeUnit.SECONDS);

                    template = new CouchbaseTemplate(cluster.clusterManager(config.getAdminUser(),
                            config.getAdminPassword()).info(), bucket);
                }
            }
        }
    }

    @Override
    public V load(K key) throws Exception {
        return template.findById(key, config.<V>getCacheType());
    }

    @Override
    public void put(K key, V value) {
        template.save(value);
    }

    @Override
    public void remove(K key) {
        try {
            template.remove(load(key));
        } catch (Exception e) {
            // FIXME - Should load really throw Exception?
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (cluster != null) {
            dropBucket();

            LOG.info("Disconnecting from Couchbase cluster {}", config.getServerAddresses());
            cluster.disconnect();
            cluster = null;
        }
    }

    @Override
    public Cluster getDriverSession() {
        return cluster;
    }

    @Override
    public String getName() {
        return config.getCacheType().getSimpleName();
    }

    private void dropBucket() {
        if (config.getSchemaOptions().shouldDropSchema() && manager.hasBucket(config.getBucketName())) {
            LOG.debug("Removing bucket {}", config.getBucketName());
            manager.removeBucket(config.getBucketName(), TIMEOUT, TimeUnit.SECONDS);
        }
    }

    private void createBucket() {
        if (config.getSchemaOptions().shouldCreateSchema() && !manager.hasBucket(config.getBucketName())) {
            LOG.debug("Creating bucket {}", config.getBucketName());
            BucketSettings settings = DefaultBucketSettings.builder()
                    .name(config.getBucketName())
                    .password(config.getBucketPassword())
                    .enableFlush(config.isFlushEnabled())
                    .quota(config.getBucketSize())
                    .replicas(config.getNumReplicas())
                    .build();

            manager.insertBucket(settings, TIMEOUT, TimeUnit.SECONDS);
        }
    }
}

