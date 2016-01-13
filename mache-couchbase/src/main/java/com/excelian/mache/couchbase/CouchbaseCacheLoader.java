package com.excelian.mache.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.AbstractCacheLoader;
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
public class CouchbaseCacheLoader<K, V> extends AbstractCacheLoader<K, V, Cluster> {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseCacheLoader.class);

    private Class<K> keyType;
    private Class<V> valueType;
    private final ConnectionContext<Cluster> connectionContext;
    private Bucket bucket;
    private ClusterManager manager;
    private CouchbaseTemplate template;
    private BucketSettings bucketSettings;

    private List<String> nodes;
    private String adminUser;
    private String adminPassword;
    private SchemaOptions schemaOptions;

    /**
     * @param keyType The class type of the cache key.
     * @param valueType The class type of the cache value.
     * @param bucketSettings Bucket that will hold cached objects.
     * @param connectionContext Cluster connection.
     * @param adminUser Administration user for Couchbase cluster.
     * @param adminPassword Password for Administration user for Couchbase cluster.
     * @param schemaOptions Determine whether to create/drop bucket.
     */
    public CouchbaseCacheLoader(Class<K> keyType, Class<V> valueType, BucketSettings bucketSettings,
                                ConnectionContext<Cluster> connectionContext, String adminUser,
                                String adminPassword, SchemaOptions schemaOptions) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.bucketSettings = bucketSettings;
        this.connectionContext = connectionContext;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.schemaOptions = schemaOptions;

        if(connectionContext==null)
        {
            throw new NullPointerException("Cluster cannot be null");
        }
    }

    @Override
    public void create() {
        synchronized (this) {
            if (manager == null) {
                LOG.info("Attempting to connect to authenticate to Couchbase cluster as {}", adminUser);
                manager = connectionContext.getStorage().clusterManager(adminUser, adminPassword);

                dropBucketIfRequired();
                bucket = createBucketIfRequired();

                template = new CouchbaseTemplate(manager.info(), bucket);
                LOG.info("Using Couchbase bucket: {}", bucket);
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
        if (manager != null) {
            synchronized (this) {
                if (manager != null) {
                    dropBucketIfRequired();
                }
            }
        }

        if (bucket != null) {
            synchronized (this) {
                if (bucket != null) {
                    bucket.close();
                    bucket=null;
                }
            }
        }

    }

    @Override
    public Cluster getDriverSession() {
        return connectionContext.getStorage();
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
        return connectionContext.getStorage().openBucket(bucketSettings.name());
    }

    @Override
    public String toString() {
        return "CouchbaseCacheLoader{"
                + "connectionContext=" + connectionContext
                + ", manager=" + manager
                + ", template=" + template
                + ", bucketSettings=" + bucketSettings
                + ", nodes=" + nodes
                + ", adminUser='" + adminUser + '\''
                + ", adminPassword='" + adminPassword + '\''
                + ", schemaOptions=" + schemaOptions
                + ", keyType=" + keyType
                + ", valueType=" + valueType
                + '}';
    }
}

