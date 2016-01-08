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
        return new ClusterBuilder() {
            @Override
            public KeyspaceBuilder withCluster(Cluster.Builder clusterBuilder) {
                return new KeyspaceBuilder() {
                    @Override
                    public CassandraProvisionerBuilder withKeyspace(String keyspace) {
                        final CassandraConnectionContext cassandraConnectionContext =
                            CassandraConnectionContext.getInstance(clusterBuilder);
                        return new CassandraProvisionerBuilder(cassandraConnectionContext, keyspace);
                    }
                };
            }
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
        private final CassandraConnectionContext connectionContext;
        private final String keySpace;
        private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;
        private String replicationClass = "SimpleStrategy";
        private int replicationFactor = 1;

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
        public class CassandraJsonProvisionerBuilder extends CassandraProvisionerBuilder {
            private String tableName;
            private String idField;

            public CassandraJsonProvisionerBuilder(CassandraProvisionerBuilder cassandraProvisionerBuilder) {
                super(cassandraProvisionerBuilder.connectionContext, cassandraProvisionerBuilder.keySpace);
            }

            public CassandraJsonProvisionerBuilder inTable(String tableName) {
                this.tableName = tableName;
                return this;
            }

            public CassandraJsonProvisionerBuilder withIDField(String idField) {
                this.idField = idField;
                return this;
            }

            public CassandraJsonProvisioner build() {
                return new CassandraJsonProvisioner(connectionContext, schemaOptions,
                    keySpace, replicationClass, replicationFactor, tableName, idField);
            }
        }
    }
}