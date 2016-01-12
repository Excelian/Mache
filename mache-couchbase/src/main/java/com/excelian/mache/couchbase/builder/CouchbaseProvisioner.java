package com.excelian.mache.couchbase.builder;

import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.CouchbaseCacheLoader;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link StorageProvisioner} implementation for Couchbase.
 */
public class CouchbaseProvisioner implements StorageProvisioner {

    private final BucketSettings bucketSettings;
    private CouchbaseEnvironment couchbaseEnvironment;
    private List<String> nodes;
    private String adminUser;
    private String adminPassword;
    private SchemaOptions schemaOptions;

    private CouchbaseProvisioner(CouchbaseEnvironment couchbaseEnvironment, BucketSettings bucketSettings,
                                 List<String> nodes, String adminUser, String adminPassword,
                                 SchemaOptions schemaOptions) {
        this.couchbaseEnvironment = couchbaseEnvironment;
        this.bucketSettings = bucketSettings;
        this.nodes = nodes;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.schemaOptions = schemaOptions;
    }

    /**
     * @return A builder for a {@link CouchbaseProvisioner}.
     */
    public static BucketBuilder couchbase() {
        return CouchbaseProvisionerBuilder::new;
    }

    @Override
    public <K, V> MacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        return new CouchbaseCacheLoader<>(keyType, valueType, bucketSettings,
                couchbaseEnvironment, nodes, adminUser, adminPassword, schemaOptions);
    }

    /**
     * Forces bucket settings to be provided.
     */
    public interface BucketBuilder {
        CouchbaseProvisionerBuilder withBucketSettings(BucketSettings bucketSettings);
    }

    /**
     * A builder with defaults for a Couchbase cluster.
     */
    public static class CouchbaseProvisionerBuilder {
        private BucketSettings bucketSettings;
        private CouchbaseEnvironment couchbaseEnvironment = null;
        private List<String> nodes = Collections.singletonList("localhost");
        private String adminUser = "Administrator";
        private String adminPassword = "password";
        private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;

        public CouchbaseProvisionerBuilder(BucketSettings bucketSettings) {
            this.bucketSettings = bucketSettings;
        }

        public CouchbaseProvisionerBuilder withCouchbaseEnvironment(CouchbaseEnvironment couchbaseEnvironment) {
            this.couchbaseEnvironment = couchbaseEnvironment;
            return this;
        }

        public CouchbaseProvisionerBuilder withNodes(String... nodes) {
            this.nodes = Arrays.stream(nodes).collect(Collectors.toList());
            return this;
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

        /**
         * Creates a CouchbaseProvisioner.
         *
         * @return the created CouchbaseProvisioner
         */
        public CouchbaseProvisioner create() {
            if (couchbaseEnvironment == null) {
                couchbaseEnvironment = DefaultCouchbaseEnvironment.create();
            }
            return new CouchbaseProvisioner(couchbaseEnvironment, bucketSettings, nodes, adminUser, adminPassword,
                    schemaOptions);
        }
    }

}