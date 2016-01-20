package com.excelian.mache.couchbase.builder;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.builder.storage.StorageProvisioner;
import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.CouchbaseCacheLoader;

/**
 * {@link StorageProvisioner} implementation for Couchbase.
 */
public class CouchbaseProvisioner implements StorageProvisioner {

    private final BucketSettings bucketSettings;
    private ConnectionContext<Cluster> connectionContext;

    private String adminUser;
    private String adminPassword;
    private SchemaOptions schemaOptions;

    private CouchbaseProvisioner(ConnectionContext<Cluster> connectionContext, BucketSettings bucketSettings,
                                 String adminUser, String adminPassword,
                                 SchemaOptions schemaOptions) {
        this.connectionContext = connectionContext;
        this.bucketSettings = bucketSettings;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.schemaOptions = schemaOptions;
    }

    public static ConnectionContext<Cluster> couchbaseConnectionContext(String contactPoint, DefaultCouchbaseEnvironment builder) {
        return new ConnectionContext<Cluster>() {

            Cluster cluster;

            @Override
            public Cluster getConnection() {
                if (cluster == null) {
                    synchronized (this) {
                        if (cluster == null) {
                            cluster = CouchbaseCluster.create(builder, contactPoint);
                        }
                    }
                }
                return cluster;
            }

            @Override
            public void close() throws Exception {
                if (cluster != null) {
                    synchronized (this) {
                        if (cluster != null) {
                            cluster.disconnect();
                            cluster = null;
                        }
                    }
                }
            }
        };
    }

    /**
     * @return A builder for a {@link CouchbaseProvisioner}.
     */
    public static ClusterBuilder couchbase() {
        return cluster -> bucket -> new CouchbaseProvisionerBuilder(cluster, bucket);
    }

    @Override
    public <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType) {
        return new MacheFactory().create(getCacheLoader(keyType, valueType));
    }

    @Override
    public <K, V> AbstractCacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        return new CouchbaseCacheLoader<>(keyType, valueType, bucketSettings, connectionContext, adminUser, adminPassword, schemaOptions);
    }

    /**
     * Forces bucket settings to be provided.
     */
    public interface ClusterBuilder {
        BucketBuilder withContext(ConnectionContext<Cluster> connectionContext);
    }

    public interface BucketBuilder {
        CouchbaseProvisionerBuilder withBucketSettings(BucketSettings bucketSettings);
    }

    /**
     * A builder with defaults for a Couchbase cluster.
     */
    public static class CouchbaseProvisionerBuilder {
        private final ConnectionContext<Cluster> connectionContext;
        private final BucketSettings bucketSettings;
        private String adminUser = "Administrator";
        private String adminPassword = "password";
        private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;

        public CouchbaseProvisionerBuilder(ConnectionContext<Cluster> connectionContext, BucketSettings bucketSettings) {
            this.connectionContext = connectionContext;
            this.bucketSettings = bucketSettings;
        }

        public CouchbaseProvisionerBuilder withAdminDetails(String adminUser, String adminPassword) {
            this.adminUser = adminUser;
            this.adminPassword = adminPassword;
            return this;
        }

        public CouchbaseProvisionerBuilder withSchemaOptions(SchemaOptions schemaOptions) {
            this.schemaOptions = schemaOptions;
            return this;
        }

        public CouchbaseProvisioner build() {
            return new CouchbaseProvisioner(connectionContext, bucketSettings, adminUser, adminPassword, schemaOptions);
        }
    }
}
