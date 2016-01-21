package com.excelian.mache.couchbase.builder;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.CouchbaseCacheLoader;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import java.util.List;

/**
 * {@link StorageProvisioner} implementation for Couchbase.
 */
public class CouchbaseProvisioner implements StorageProvisioner {

    private final CouchbaseConnectionContext couchbaseConnectionContext;
    private final BucketSettings bucketSettings;
    private ConnectionContext<Cluster> connectionContext;

    private String adminUser;
    private String adminPassword;
    private SchemaOptions schemaOptions;

    private CouchbaseProvisioner(CouchbaseConnectionContext couchbaseConnectionContext, BucketSettings bucketSettings,
                                 String adminUser, String adminPassword,
                                 SchemaOptions schemaOptions) {
        this.couchbaseConnectionContext = couchbaseConnectionContext;
        this.bucketSettings = bucketSettings;
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
    public <K, V> MacheLoader<K, V> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        return new CouchbaseCacheLoader<>(keyType, valueType, bucketSettings, connectionContext, adminUser,
                adminPassword, schemaOptions);
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
        private final BucketSettings bucketSettings;
        private CouchbaseEnvironment couchbaseEnvironment = DefaultCouchbaseEnvironment.create();
        private List<String> nodes = singletonList("localhost");
        private String adminUser = "Administrator";
        private String adminPassword = "password";
        private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;

        /**
         * @param bucketSettings the mandatory bucket settings.
         */
        public CouchbaseProvisionerBuilder(BucketSettings bucketSettings) {
            this.bucketSettings = bucketSettings;
        }

        public CouchbaseProvisionerBuilder withCouchbaseEnvironment(CouchbaseEnvironment couchbaseEnvironment) {
            this.couchbaseEnvironment = couchbaseEnvironment;
            return this;
        }

        public CouchbaseProvisionerBuilder withNodes(String... nodes) {
            this.nodes = stream(nodes).collect(toList());
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

        public CouchbaseProvisioner build() {
            final CouchbaseConnectionContext connectionContext = CouchbaseConnectionContext.getInstance(couchbaseEnvironment, nodes);
            return new CouchbaseProvisioner(connectionContext, bucketSettings, adminUser, adminPassword, schemaOptions);
        }
    }
}
