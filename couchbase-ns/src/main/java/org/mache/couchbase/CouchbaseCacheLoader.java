package org.mache.couchbase;

import com.couchbase.client.ClusterManager;
import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.clustermanager.BucketType;
import org.mache.AbstractCacheLoader;
import org.mache.SchemaOptions;
import org.springframework.data.couchbase.core.CouchbaseOperations;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import java.io.IOException;
import java.util.Properties;

// FIXME - a Spring Data update for Couchbase 2.1 SDK will soon be available. This loader should be updated when it is.
public class CouchbaseCacheLoader<K extends String, V> extends AbstractCacheLoader<K, V, CouchbaseClient> {

    private CouchbaseClient client;
    private CouchbaseConfig config;

    static {
        // Use SLF4J logging.
        Properties systemProperties = System.getProperties();
        systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SLF4JLogger");
        System.setProperties(systemProperties);
    }

    public CouchbaseCacheLoader(CouchbaseConfig config) {
        this.config = config;
    }

    @Override
    public void create(String name, K key) {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    try {
                        ClusterManager manager = new ClusterManager(config.getServerAddresses(),
                                config.getAdminUser(), config.getAdminPassword());

                        try {
                            SchemaOptions schemaOptions = config.getSchemaOptions();
                            String bucketName = config.getBucketName();

                            if (schemaOptions.ShouldDropSchema() && manager.listBuckets().contains(bucketName)) {
                                manager.deleteBucket(bucketName);
                            }

                            if (schemaOptions.ShouldCreateSchema() && !manager.listBuckets().contains(bucketName)) {
                                manager.createNamedBucket(BucketType.COUCHBASE, bucketName, config.getBucketSize(),
                                        config.getNumReplicas(), config.getBucketPassword(), config.isFlushEnabled());
                            }

                            try {
                                /*
                                    TODO - query for when the bucket is ready.

                                    The cluster manager in the Couchbase 1.4 SDK creates buckets via a REST API.
                                    It returns before the bucket is finished, and if you try to create a client to
                                    it before it's finished, you'll get a warning in the logs. I can't find a way
                                    of querying if it's finished (listBuckets returns it, flushing it directly after
                                    creating throws 503), so just have a wait here for now.

                                    Likely moot when new couchbase spring data is available.
                                */
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                // NO OP
                            }

                            client = new CouchbaseClient(config.getServerAddresses(), bucketName,
                                    config.getBucketPassword());

                        } finally {
                            manager.shutdown();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    public V load(K key) throws Exception {
        return ops().findById(key, config.<V> getCacheType());
    }

    @Override
    public void put(K key, V value) {
        ops().save(value);
    }

    @Override
    public void remove(K key) {
        ops().remove(ops().findById(key, config.getCacheType()));
    }

    @Override
    public void close() {
        if (client != null) {
            if (config.getSchemaOptions().ShouldDropSchema()) {
                ClusterManager manager = new ClusterManager(config.getServerAddresses(), config.getAdminUser(),
                        config.getAdminPassword());
                manager.deleteBucket(config.getBucketName());
            }

            client.shutdown();
        }
    }

    private CouchbaseOperations ops() {
        return new CouchbaseTemplate(client);
    }

    @Override
    public CouchbaseClient getDriverSession() {
        return client;
    }

    @Override
    public String getName() {
        return config.getCacheType().getSimpleName();
    }
}

