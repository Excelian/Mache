package com.excelian.mache.cassandra.builder;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.excelian.mache.core.SchemaOptions;

/**
 * {@link StorageProvisioner} implementation for Cassandra.
 */
public class CassandraProvisioner implements StorageProvisioner {

    private final ConnectionContext<Cluster> connectionContext;
    private final SchemaOptions schemaOptions;
    private final String keySpace;
    private final String replicationClass;
    private final int replicationFactor;

    private CassandraProvisioner(ConnectionContext<Cluster> connectionContext, SchemaOptions schemaOptions, String keySpace,
                                 String replicationClass, int replicationFactor) {
        this.connectionContext = connectionContext;
        this.schemaOptions = schemaOptions;
        this.keySpace = keySpace;
        this.replicationClass = replicationClass;
        this.replicationFactor = replicationFactor;
    }

    /**
     * @return A builder for a {@link CassandraProvisioner}.
     */
    public static ClusterBuilder cassandra() {
        return clusterBuilder -> keyspace -> {
            final CassandraConnectionContext cassandraConnectionContext = CassandraConnectionContext.getInstance(clusterBuilder);
            return new CassandraProvisionerBuilder(cassandraConnectionContext, keyspace);
        };
    }

    @Override
    public <K, V> CassandraCacheLoader<K, V> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        return new CassandraCacheLoader<>(keyType, valueType, connectionContext,
                schemaOptions, keySpace, replicationClass, replicationFactor);
    }

    /**
     * Forces cluster settings to be provided.
     */
    public interface ClusterBuilder {
        KeyspaceBuilder withCluster(Cluster.Builder clusterBuilder);
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
        private final ConnectionContext<Cluster> connectionContext;
        private final String keySpace;
        private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;
        private String replicationClass = "SimpleStrategy";
        private int replicationFactor = 1;

        private CassandraProvisionerBuilder(ConnectionContext<Cluster> connectionContext, String keySpace) {
            this.connectionContext = connectionContext;
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
            return new CassandraProvisioner(connectionContext, schemaOptions, keySpace, replicationClass, replicationFactor);
        }
    }
}