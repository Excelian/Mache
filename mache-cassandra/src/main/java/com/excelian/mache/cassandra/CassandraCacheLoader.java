package com.excelian.mache.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.AbstractCacheLoader;
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
public class CassandraCacheLoader<K, V> extends AbstractCacheLoader<K, V, Session> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraCacheLoader.class);
    private static final String CREATE_KEYPSACE = "CREATE KEYSPACE IF NOT EXISTS %s  "
            + "WITH REPLICATION = {'class':'%s', 'replication_factor':%d}; ";

    private final Class<K> keyType;
    private final Class<V> valueType;
    private final ConnectionContext<Cluster> connectionContext;
    private final SchemaOptions schemaOption;
    private final String replicationClass;
    private final int replicationFactor;
    private final String keySpace;
    private Session session;
    private boolean isTableCreated = false;

    /**
     * @param keyType           The class type of the cache key.
     * @param valueType         The class type of the cache value.
     * @param connectionContext           The Cassandra cluster object that defines cluster parameters.
     * @param schemaOption      Determine whether to create/drop key space.
     * @param keySpace          The name of the key space to use.
     * @param replicationClass  The type of replication strategy to use for the key space.
     * @param replicationFactor The replication factor for the keyspace.
     */
    public CassandraCacheLoader(Class<K> keyType, Class<V> valueType, ConnectionContext<Cluster> connectionContext, SchemaOptions schemaOption,
                                String keySpace, String replicationClass, int replicationFactor) {
        this.keyType = keyType;
        this.connectionContext = connectionContext;
        this.schemaOption = schemaOption;
        this.replicationClass = replicationClass;
        this.replicationFactor = replicationFactor;
        this.keySpace = keySpace.replace("-", "_").replace(" ", "_").replace(":", "_");
        this.valueType = valueType;
    }

    @Override
    public void create() {
        if (schemaOption.shouldCreateSchema() && session == null) {
            synchronized (this) {
                if (session == null) {
                    session = connectionContext.getConnection(this).connect();
                    if (schemaOption.shouldCreateSchema()) {
                        createKeySpace();
                    }
                    createTable();
                }
            }
        } else {
            session = connectionContext.getConnection(this).connect(keySpace);
        }
    }

    private void createKeySpace() {
        session.execute(String.format(CREATE_KEYPSACE, keySpace, replicationClass, replicationFactor));
        session.execute(String.format("USE %s", keySpace));
        LOG.info("Created keyspace if missing {}", keySpace);
    }

    private void createTable() {
        if (!isTableCreated) {
            CassandraAdminTemplate adminTemplate = new CassandraAdminTemplate(session, new MappingCassandraConverter());
            adminTemplate.createTable(true, null, valueType, null);
            isTableCreated = true;
        }
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
    public void close() {
        if (session != null && !session.isClosed()) {
            if (schemaOption.shouldDropSchema()) {
                try {
                    session.execute(String.format("DROP KEYSPACE %s; ", keySpace));
                    LOG.info("Dropped keyspace {}", keySpace);
                } catch (DriverException e) {
                    LOG.error("Failed to drop keyspace : {}. err={}", keySpace, e);
                }
            }
            session.close();
            session=null;
        }
    }

    @Override
    public String getName() {
        return valueType.getSimpleName();
    }

    private CassandraOperations ops() {
        return new CassandraTemplate(session);
    }

    @Override
    public String toString() {
        return "CassandraCacheLoader{"
                + "connectionContext=" + connectionContext
                + ", schemaOption=" + schemaOption
                + ", replicationClass='" + replicationClass + '\''
                + ", replicationFactor=" + replicationFactor
                + ", keySpace='" + keySpace + '\''
                + ", session=" + session
                + ", isTableCreated=" + isTableCreated
                + ", keyType=" + keyType
                + ", valueType=" + valueType
                + '}';
    }
}