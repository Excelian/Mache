package com.excelian.mache.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.excelian.mache.cassandra.builder.CassandraConnectionContext;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base CacheLoader to bind Cassandra API onto the GuavaCache
 *
 * @param <K> Cache key type.
 * @param <V> Cache value type.
 */
public abstract class AbstractCassandraCacheLoader<K, V> implements MacheLoader<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCassandraCacheLoader.class);
    private static final String CREATE_KEYSPACE = "CREATE KEYSPACE IF NOT EXISTS %s  "
        + "WITH REPLICATION = {'class':'%s', 'replication_factor':%d}; ";

    private final Class<K> keyType;
    protected final Class<V> valueType;
    private final CassandraConnectionContext connectionContext;
    private final SchemaOptions schemaOption;
    private final String replicationClass;
    private final int replicationFactor;
    private final String keySpace;
    protected Session session;

    /**
     * @param keyType           The class type of the cache key.
     * @param valueType         The class type of the cache value.
     * @param connectionContext The Cassandra cluster object that defines cluster parameters.
     * @param schemaOption      Determine whether to create/drop key space.
     * @param keySpace          The name of the key space to use.
     * @param replicationClass  The type of replication strategy to use for the key space.
     * @param replicationFactor The replication factor for the keyspace.
     */
    public AbstractCassandraCacheLoader(Class<K> keyType, Class<V> valueType,
                                        CassandraConnectionContext connectionContext,
                                        SchemaOptions schemaOption, String keySpace,
                                        String replicationClass, int replicationFactor) {
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
                    if (shouldCreateTable()) {
                        createTable();
                    }
                }
            }
        } else {
            session = connectionContext.getConnection(this).connect(keySpace);
        }
    }

    protected void createKeySpace() {
        session.execute(String.format(CREATE_KEYSPACE, keySpace, replicationClass, replicationFactor));
        session.execute(String.format("USE %s", keySpace));
        LOG.info("Created keyspace if missing {}", keySpace);
    }

    protected abstract boolean shouldCreateTable();

    protected abstract void createTable();

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
            session = null;
        }
        this.connectionContext.close(this);
    }


    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
            + "connectionContext=" + connectionContext
            + ", schemaOption=" + schemaOption
            + ", replicationClass='" + replicationClass + '\''
            + ", replicationFactor=" + replicationFactor
            + ", keySpace='" + keySpace + '\''
            + ", session=" + session
            + ", keyType=" + keyType
            + ", valueType=" + valueType
            + '}';
    }
}