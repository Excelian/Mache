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

/**
 * An implementation of the Mache CacheLoader for Couchbase Server. Utilises the newer Spring Data Couchbase
 * that
 *
 * @param <K> Cache key type.
 * @param <V> Cache value type.
 */
public class CouchbaseCacheLoader<K, V> extends AbstractCacheLoader<K, V, Cluster> {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseCacheLoader.class);
    public static final long TIMEOUT = 20;

    private Cluster cluster;
    private ClusterManager manager;
    private final CouchbaseConfig config;
    private CouchbaseTemplate template;

    /**
     *
     * @param config {@link CouchbaseConfig} representing the Couchbase server and bucket config.
     */
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
                    dropBucketIfRequired();
                    Bucket bucket = createBucket();
                    template = new CouchbaseTemplate(manager.info(), bucket);
                    LOG.info("Using Couchbase bucket: {}", bucket);
                }
            }
        }
    }

    @Override
    public V load(K key) throws Exception {
        return template.findById(key.toString(), config.<V>getCacheType());
    }

    @Override
    public void put(K key, V value) {
        template.save(value);
    }

    @Override
    public void remove(K key) {
        template.remove(key);
    }

    @Override
    public void close() {
        if (cluster != null) {
            synchronized (this) {
                if (cluster != null) {
                    dropBucketIfRequired();
                    LOG.info("Disconnecting from Couchbase cluster {}", config.getServerAddresses());
                    cluster.disconnect();
                    cluster = null;
                }
            }
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

    private void dropBucketIfRequired() {
        if (config.getSchemaOptions().shouldDropSchema() && manager.hasBucket(config.getBucketName())) {
            LOG.debug("Removing bucket {}", config.getBucketName());
            manager.removeBucket(config.getBucketName(), TIMEOUT, TimeUnit.SECONDS);
        }
    }

    private Bucket createBucket() {
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

        return cluster.openBucket(config.getBucketName(), TIMEOUT, TimeUnit.SECONDS);
    }
}

