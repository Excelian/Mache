package com.excelian.mache.cassandra.builder;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.excelian.mache.core.SchemaOptions;

/**
 * {@link StorageProvisioner} implementation for Cassandra.
 */
public class CassandraProvisioner implements StorageProvisioner {

    private final Cluster cluster;
    private final SchemaOptions schemaOptions;
    private final String keySpace;
    private final String replicationClass;
    private final int replicationFactor;

    private CassandraProvisioner(Cluster cluster, SchemaOptions schemaOptions, String keySpace,
                                 String replicationClass, int replicationFactor) {
        this.cluster = cluster;
        this.schemaOptions = schemaOptions;
        this.keySpace = keySpace;
        this.replicationClass = replicationClass;
        this.replicationFactor = replicationFactor;
    }

    @Override
    public <K, V> CassandraCacheLoader<K, V> getCacheLoader(Class<K> keyType, Class<V> valueType) {
    	return new CassandraCacheLoader<>(keyType, valueType, cluster,
                schemaOptions, keySpace, replicationClass, replicationFactor);
    }

    /**
     * @return A builder for a {@link CassandraProvisioner}.
     */
    public static ClusterBuilder cassandra() {
        return cluster -> keyspace -> new CassandraProvisionerBuilder(cluster, keyspace);
    }

    /**
     * Forces cluster settings to be provided.
     */
    public interface ClusterBuilder {
        KeyspaceBuilder withCluster(Cluster cluster);
    }

    /**
     * Forces a keyspace name to be provided.
     */
    public interface KeyspaceBuilder {
        CassandraProvisionerBuilder withKeyspace(String keySpace);
    }

    /**
     * A builder with defaults for a Cassandra cluster.
     */
    public static class CassandraProvisionerBuilder {
        private final Cluster cluster;
        private final String keySpace;
        private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;
        private String replicationClass = "SimpleStrategy";
        private int replicationFactor = 1;

        private CassandraProvisionerBuilder(Cluster cluster, String keySpace) {
            this.cluster = cluster;
            this.keySpace = keySpace;
        }

        public CassandraProvisionerBuilder withSchemaOptions(SchemaOptions schemaOptions) {
            this.schemaOptions = schemaOptions;
            return this;
        }

        public CassandraProvisionerBuilder withReplicationClass(String replicationClass) {
            this.replicationClass = replicationClass;
            return this;
        }

        public CassandraProvisionerBuilder withReplicationFactor(int replicationFactor) {
            this.replicationFactor = replicationFactor;
            return this;
        }

        public CassandraProvisioner build() {
            return new CassandraProvisioner(cluster, schemaOptions, keySpace, replicationClass, replicationFactor);
        }
    }
}