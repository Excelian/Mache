package com.excelian.mache.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.builder.CouchbaseConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An abstract base class for the Mache CacheLoader for Couchbase Server.
 * Encapsulates common functionality for all Couchbase cache loaders.
 *
 * @param <K> Cache key type.
 * @param <V> Cache value type.
 */
public abstract class AbstractCouchbaseCacheLoader<K, V> implements MacheLoader<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCouchbaseCacheLoader.class);
    private final CouchbaseConnectionContext connectionContext;
    protected Bucket bucket;
    protected ClusterManager manager;
    private BucketSettings bucketSettings;
    private String adminUser;
    private String adminPassword;
    private SchemaOptions schemaOptions;

    /**
     * @param bucketSettings    Bucket that will hold cached objects.
     * @param connectionContext Cluster connection.
     * @param adminUser         Administration user for Couchbase cluster.
     * @param adminPassword     Password for Administration user for Couchbase cluster.
     * @param schemaOptions     Determine whether to create/drop bucket.
     */
    public AbstractCouchbaseCacheLoader(BucketSettings bucketSettings,
                                        CouchbaseConnectionContext connectionContext, String adminUser,
                                        String adminPassword, SchemaOptions schemaOptions) {
        this.bucketSettings = bucketSettings;
        this.connectionContext = connectionContext;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.schemaOptions = schemaOptions;
    }

    @Override
    public void create() {
        synchronized (this) {
            if (manager == null) {
                LOG.info("Attempting to connect to authenticate to Couchbase cluster as {}", adminUser);
                manager = connectionContext.getConnection(this).clusterManager(adminUser, adminPassword);

                dropBucketIfRequired();
                bucket = createBucketIfRequired();

                LOG.info("Using Couchbase bucket: {}", bucket);
            }
        }
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
                    bucket = null;
                }
            }
        }
        connectionContext.close(this);
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
        return connectionContext.getConnection(this).openBucket(bucketSettings.name());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{"
            + "connectionContext=" + connectionContext
            + ", manager=" + manager
            + ", bucketSettings=" + bucketSettings
            + ", adminUser='" + adminUser + '\''
            + ", adminPassword='" + adminPassword + '\''
            + ", schemaOptions=" + schemaOptions
            + '}';
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}

