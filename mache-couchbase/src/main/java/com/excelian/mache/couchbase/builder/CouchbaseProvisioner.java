package com.excelian.mache.couchbase.builder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
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
    private Cluster cluster;

    private String adminUser;
    private String adminPassword;
    private SchemaOptions schemaOptions;

    private CouchbaseProvisioner(Cluster cluster, BucketSettings bucketSettings,
                                 String adminUser, String adminPassword,
                                 SchemaOptions schemaOptions) {
        this.cluster = cluster;
        this.bucketSettings = bucketSettings;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.schemaOptions = schemaOptions;
    }

    @Override
    public <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType) {
        return new MacheFactory().create(getCacheLoader(keyType, valueType));
    }
    
    @Override
    public <K, V> AbstractCacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
    	return new CouchbaseCacheLoader<>(keyType, valueType, bucketSettings, cluster, adminUser, adminPassword, schemaOptions);
    }

    /**
     * @return A builder for a {@link CouchbaseProvisioner}.
     */
    public static ClusterBuilder couchbase() {
        return new CouchbaseProvisionerBuilder();
    }

    /**
     * Forces bucket settings to be provided.
     */
    public interface ClusterBuilder {
        BucketBuilder withCluster(Cluster cluster);
    }

    public interface BucketBuilder {
        AdminSettings withBucketSettings(BucketSettings bucketSettings);
    }

    public interface AdminSettings {
        SchemaOptionsSettings withAdminDetails(String adminUser, String adminPassword);
        SchemaOptionsSettings withDefaultAdminDetails();
    }

    public interface SchemaOptionsSettings {
        CouchbaseProvisionerBuilder withSchemaOptions(SchemaOptions schemaOptions);
        CouchbaseProvisionerBuilder withDefaultSchemaOptions();
    }

    /**
     * A builder with defaults for a Couchbase cluster.
     */
    public static class CouchbaseProvisionerBuilder implements ClusterBuilder,BucketBuilder,AdminSettings,SchemaOptionsSettings
    {
        private Cluster cluster;
        private BucketSettings bucketSettings;
        private String adminUser = "Administrator";
        private String adminPassword = "password";
        private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;

        public BucketBuilder withCluster(Cluster cluster) {
            this.cluster = cluster;
            return this;
        }

        public SchemaOptionsSettings withAdminDetails(String adminUser, String adminPassword) {
            this.adminUser = adminUser;
            this.adminPassword = adminPassword;
            return this;
        }

        @Override
        public SchemaOptionsSettings withDefaultAdminDetails() {
            adminUser = "Administrator";
            adminPassword = "password";
            return this;
        }

        public CouchbaseProvisionerBuilder withSchemaOptions(SchemaOptions schemaOptions) {
            this.schemaOptions = schemaOptions;
            return this;
        }

        @Override
        public CouchbaseProvisionerBuilder withDefaultSchemaOptions() {
            schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;
            return this;
        }

        @Override
        public AdminSettings withBucketSettings(BucketSettings bucketSettings) {
            this.bucketSettings=bucketSettings;
            return this;
        }

        public CouchbaseProvisioner build() {
            if(cluster==null)
            {
                throw new NullPointerException("Cannot build without a cluster defined");
            }
            return new CouchbaseProvisioner(cluster, bucketSettings, adminUser, adminPassword, schemaOptions);
        }

    }

}