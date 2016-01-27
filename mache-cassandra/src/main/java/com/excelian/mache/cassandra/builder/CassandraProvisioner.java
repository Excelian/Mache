package com.excelian.mache.cassandra.builder;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.excelian.mache.core.SchemaOptions;

/**
 * {@link StorageProvisioner} implementation for Cassandra.
 */
public class CassandraProvisioner implements StorageProvisioner {

    private final CassandraConnectionContext connectionContext;
    private final SchemaOptions schemaOptions;
    private final String keySpace;
    private final String replicationClass;
    private final int replicationFactor;

    /**
     * Constructor.
     *
     * @param connectionContext - the central storage of managed resources
     * @param schemaOptions     - policy for schema creation
     * @param keySpace          - where to store the tables
     * @param replicationClass  - Cassandra replication class
     * @param replicationFactor - Cassandra replication factor
     */
    protected CassandraProvisioner(CassandraConnectionContext connectionContext,
                                   SchemaOptions schemaOptions, String keySpace,
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
            final CassandraConnectionContext cassandraConnectionContext =
                CassandraConnectionContext.getInstance(clusterBuilder);
            return new CassandraProvisionerBuilder(cassandraConnectionContext, keyspace);
        };
    }

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
        protected final CassandraConnectionContext connectionContext;
        protected final String keySpace;
        protected SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;
        protected String replicationClass = "SimpleStrategy";
        protected int replicationFactor = 1;

        private CassandraProvisionerBuilder(CassandraConnectionContext connectionContext, String keySpace) {
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

        public StorageProvisioner build() {
            return new CassandraProvisioner(connectionContext, schemaOptions,
                keySpace, replicationClass, replicationFactor);
        }

        public CassandraJsonProvisionerBuilder asJsonDocuments() {
            return new CassandraJsonProvisionerBuilder(this);
        }


        /**
         * Provisions Cassandra Cache Loaders that can persist and serve values
         * as JSON Strings.
         */
        public static class CassandraJsonProvisionerBuilder extends CassandraProvisionerBuilder {

            public CassandraJsonProvisionerBuilder(CassandraProvisionerBuilder cassandraProvisionerBuilder) {
                super(cassandraProvisionerBuilder.connectionContext, cassandraProvisionerBuilder.keySpace);
            }

            public CassandraJsonTableProvisionerBuilder inTable(String tableName) {
                return idField ->
                    new CassandraJsonProvisioner(connectionContext, schemaOptions,
                        keySpace, replicationClass, replicationFactor, tableName, idField);
            }

            /**
             * Allows specification of ID/primary key field for the JSON doc
             * store.
             */
            public interface CassandraJsonTableProvisionerBuilder {
                CassandraJsonProvisioner withIDField(String idField);
            }
        }
    }
}