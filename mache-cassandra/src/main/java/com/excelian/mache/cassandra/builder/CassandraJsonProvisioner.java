package com.excelian.mache.cassandra.builder;

import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.cassandra.CassandraJsonCacheLoader;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;

/**
 * Provisions a CassandraCacheLoader to store String keys to String values which
 * are assumed to be Json documents).
 */
public class CassandraJsonProvisioner implements StorageProvisioner<String, String> {
    private final CassandraConnectionContext connectionContext;
    private final SchemaOptions schemaOptions;
    private final String keySpace;
    private final String replicationClass;
    private final int replicationFactor;
    private final String tableName;
    private final String idField;

    /**
     * Constructor.
     *
     * @param connectionContext The Connection context that manages the shared
     *                          Cassandra resources.
     * @param schemaOptions     Determine whether to create/drop key space.
     * @param keySpace          The name of the key space to use.
     * @param replicationClass  The type of replication strategy to use for the key space.
     * @param replicationFactor The replication factor for the keyspace.
     * @param tableName         The destination table.
     * @param idField           The destination primary key column in the destination table.
     */
    public CassandraJsonProvisioner(CassandraConnectionContext connectionContext,
                                    SchemaOptions schemaOptions, String keySpace,
                                    String replicationClass, int replicationFactor,
                                    String tableName, String idField) {
        this.connectionContext = connectionContext;
        this.schemaOptions = schemaOptions;
        this.keySpace = keySpace;
        this.replicationClass = replicationClass;
        this.replicationFactor = replicationFactor;
        this.tableName = tableName;
        this.idField = idField;
    }

    @Override
    public MacheLoader<String, String> getCacheLoader(Class<String> keyType, Class<String> valueType) {
        return new CassandraJsonCacheLoader(connectionContext,
                schemaOptions, keySpace, replicationClass, replicationFactor, tableName, idField);
    }
}
