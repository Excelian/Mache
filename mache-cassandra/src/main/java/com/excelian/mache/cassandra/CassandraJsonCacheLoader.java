package com.excelian.mache.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.DriverException;
import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.SchemaOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;

/**
 * CacheLoader to bind Cassandra API onto the GuavaCache
 *
 * TODO: Issues @bluemonk3y
 * 1. Table name is assumed to be part of the key - this isnt great because it assumes the entity relationship.
 * 2. IDField - is current hardcoded to 'id' - see
 *
 * FIX: Use Configurator/Builder to inject it and make work properly
 *
 */
public class CassandraJsonCacheLoader<K, V> extends AbstractCacheLoader<String, String, Session> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraJsonCacheLoader.class);
    private static final String CREATE_KEYPSACE = "CREATE KEYSPACE IF NOT EXISTS %s  "
            + "WITH REPLICATION = {'class':'%s', 'replication_factor':%d}; ";

    // TODO - remove this an inject using configurator
    private static final String ID_FIELD = "id";

    private final Cluster cluster;
    private final SchemaOptions schemaOption;
    private final String replicationClass;
    private final int replicationFactor;
    private final String keySpace;
    private Session session;

    /**
     * @param cluster           The Cassandra cluster object that defines cluster parameters.
     * @param schemaOption      Determine whether to create/drop key space.
     * @param keySpace          The name of the key space to use.
     * @param replicationClass  The type of replication strategy to use for the key space.
     * @param replicationFactor The replication factor for the keyspace.
     */
    public CassandraJsonCacheLoader(Cluster cluster, SchemaOptions schemaOption,
                                    String keySpace, String replicationClass, int replicationFactor) {
        this.cluster = cluster;
        this.schemaOption = schemaOption;
        this.replicationClass = replicationClass;
        this.replicationFactor = replicationFactor;
        this.keySpace = keySpace.replace("-", "_").replace(" ", "_").replace(":", "_");
    }

    @Override
    public void create() {
        session = cluster.connect(keySpace);
    }

    public void put(String key, String value) {
        String table = key.substring(0, key.indexOf("."));
        session.execute(String.format("INSERT INTO %s JSON '%s'", table, value));
    }

    public void remove(String key) {
        String[] tableKey = key.split("\\.");
        session.execute(String.format("DELETE from %s WHERE id = '%s';", tableKey[0], tableKey[1]));
    }

    public String load(String key) throws Exception {
        String[] tableKey = key.split("\\.");
        ResultSet execute = session.execute(String.format("SELECT JSON * from %s WHERE %s = '%s';",  tableKey[0], ID_FIELD, tableKey[1]));
        return  execute.one().getString(0);
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
            cluster.close();
        }
    }

    @Override
    public String getName() {
        return "Json";
    }

    @Override
    public Session getDriverSession() {
        if (session == null) {
            throw new IllegalStateException("Session has not been created - read/write to cache first");
        }
        return session;
    }

    private CassandraOperations ops() {
        return new CassandraTemplate(session);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{"
                + "cluster=" + cluster
                + ", schemaOption=" + schemaOption
                + ", replicationClass='" + replicationClass + '\''
                + ", replicationFactor=" + replicationFactor
                + ", keySpace='" + keySpace + '\''
                + ", session=" + session
                + '}';
    }
}