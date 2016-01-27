package com.excelian.mache.cassandra;

import com.excelian.mache.cassandra.builder.CassandraConnectionContext;
import com.excelian.mache.core.SchemaOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cassandra.core.ConsistencyLevel;
import org.springframework.cassandra.core.RetryPolicy;
import org.springframework.cassandra.core.WriteOptions;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

/**
 * CacheLoader to bind Cassandra API onto the GuavaCache
 *
 * @param <K> Cache key type.
 * @param <V> Cache value type.
 */
public class CassandraCacheLoader<K, V> extends AbstractCassandraCacheLoader<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraCacheLoader.class);
    private boolean isTableCreated;


    /**
     * @param keyType           The class type of the cache key.
     * @param valueType         The class type of the cache value.
     * @param connectionContext The Cassandra cluster object that defines cluster parameters.
     * @param schemaOption      Determine whether to create/drop key space.
     * @param keySpace          The name of the key space to use.
     * @param replicationClass  The type of replication strategy to use for the key space.
     * @param replicationFactor The replication factor for the keyspace.
     */
    public CassandraCacheLoader(Class<K> keyType, Class<V> valueType,
                                CassandraConnectionContext connectionContext,
                                SchemaOptions schemaOption, String keySpace,
                                String replicationClass, int replicationFactor) {
        super(keyType, valueType, connectionContext, schemaOption, keySpace, replicationClass, replicationFactor);
    }

    @Override
    protected boolean shouldCreateTable() {
        return !isTableCreated;
    }

    protected void createTable() {
        CassandraAdminTemplate adminTemplate = new CassandraAdminTemplate(session, new MappingCassandraConverter());
        adminTemplate.createTable(true, null, valueType, null);
        isTableCreated = true;
    }

    public void put(K key, V value) {
        ops().insert(value, new WriteOptions(ConsistencyLevel.LOCAL_QUOROM, RetryPolicy.DEFAULT));
    }

    public void remove(K key) {
        ops().deleteById(valueType, key);
    }

    @Override
    public V load(K key) throws Exception {
        V value = ops().selectOneById(valueType, key);
        LOG.trace("Loaded value from DB : {}", key);
        return value;
    }

    @Override
    public String getName() {
        return valueType.getSimpleName();
    }

    private CassandraOperations ops() {
        return new CassandraTemplate(session);
    }

}