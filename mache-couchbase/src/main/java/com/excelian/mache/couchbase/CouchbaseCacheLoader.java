package com.excelian.mache.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of the Mache CacheLoader for Couchbase Server. Utilises the newer Spring Data Couchbase
 * that
 *
 * @param <K> Cache key type.
 * @param <V> Cache value type.
 */
public class CouchbaseCacheLoader<K, V> implements MacheLoader<K, V, Cluster> {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseCacheLoader.class);

    private Class<K> keyType;
    private Class<V> valueType;
    private volatile Cluster cluster;
    private ClusterManager manager;
    private CouchbaseTemplate template;
    private BucketSettings bucketSettings;
    private CouchbaseEnvironment couchbaseEnvironment;
    private List<String> nodes;
    private String adminUser;
    private String adminPassword;
    private SchemaOptions schemaOptions;

    /**
     * @param keyType The class type of the cache key.
     * @param valueType The class type of the cache value.
     * @param bucketSettings Bucket that will hold cached objects.
     * @param couchbaseEnvironment Cluster environment properties.
     * @param nodes List of nodes to connect to.
     * @param adminUser Administration user for Couchbase cluster.
     * @param adminPassword Password for Administration user for Couchbase cluster.
     * @param schemaOptions Determine whether to create/drop bucket.
     */
    public CouchbaseCacheLoader(Class<K> keyType, Class<V> valueType, BucketSettings bucketSettings,
                                CouchbaseEnvironment couchbaseEnvironment, List<String> nodes, String adminUser,
                                String adminPassword, SchemaOptions schemaOptions) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.bucketSettings = bucketSettings;
        this.couchbaseEnvironment = couchbaseEnvironment;
        this.nodes = nodes;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.schemaOptions = schemaOptions;
    }

    @Override
    public void create() {
        if (cluster == null) {
            synchronized (this) {
                if (cluster == null) {
                    LOG.info("Attempting to connect to Couchbase cluster hosted at {}", nodes);
                    cluster = CouchbaseCluster.create(couchbaseEnvironment, nodes);
                    manager = cluster.clusterManager(adminUser, adminPassword);
                    dropBucketIfRequired();
                    Bucket bucket = createBucketIfRequired();
                    template = new CouchbaseTemplate(manager.info(), bucket);
                    LOG.info("Using Couchbase bucket: {}", bucket);
                }
            }
        }
    }

    @Override
    public V load(K key) throws Exception {
        checkNotNull(key);
        return template.findById(key.toString(), valueType);
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
                    LOG.info("Disconnecting from Couchbase cluster {}", nodes);
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
        return getClass().getSimpleName();
    }

    private void dropBucketIfRequired() {
        if (schemaOptions.shouldDropSchema() && manager.hasBucket(bucketSettings.name())) {
            LOG.info("Removing bucket {}", bucketSettings.name());
            manager.removeBucket(bucketSettings.name());
        }
    }

    private Bucket createBucketIfRequired() {
        if (schemaOptions.shouldCreateSchema() && !manager.hasBucket(bucketSettings.name())) {
            LOG.info("Creating bucket {}", bucketSettings.name());
            manager.insertBucket(bucketSettings);
        }
        return cluster.openBucket(bucketSettings.name());
    }

    @Override
    public String toString() {
        return "CouchbaseCacheLoader{"
                + "cluster=" + cluster
                + ", manager=" + manager
                + ", template=" + template
                + ", bucketSettings=" + bucketSettings
                + ", couchbaseEnvironment=" + couchbaseEnvironment
                + ", nodes=" + nodes
                + ", adminUser='" + adminUser + '\''
                + ", adminPassword='" + adminPassword + '\''
                + ", schemaOptions=" + schemaOptions
                + ", keyType=" + keyType
                + ", valueType=" + valueType
                + '}';
    }
}

