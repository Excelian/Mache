package com.excelian.mache.cassandra;

import com.datastax.driver.core.ResultSet;
import com.excelian.mache.cassandra.builder.CassandraConnectionContext;
import com.excelian.mache.core.SchemaOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CacheLoader to bind Cassandra API onto the GuavaCache.  Only stores Strings
 * to Strings as the value strings are JSON documents.
 */
public class CassandraJsonCacheLoader extends AbstractCassandraCacheLoader<String, String> {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraJsonCacheLoader.class);

    private final String table;
    private final String idColumn;
    private final String keyspaceDotTable;

    /**
     * Constructor.
     *
     * @param connectionContext The Connection context that manages the shared
     *                          Cassandra resources.
     * @param schemaOption      Determine whether to create/drop key space.
     * @param keySpace          The name of the key space to use.
     * @param replicationClass  The type of replication strategy to use for the key space.
     * @param replicationFactor The replication factor for the keyspace.
     * @param table             The destination table.
     * @param idColumn          The destination primary key column in the destination table.
     */
    public CassandraJsonCacheLoader(CassandraConnectionContext connectionContext,
                                    SchemaOptions schemaOption,
                                    String keySpace, String replicationClass,
                                    int replicationFactor, String table,
                                    String idColumn) {
        super(String.class, String.class, connectionContext, schemaOption,
            keySpace, replicationClass, replicationFactor);
        this.table = table;
        this.keyspaceDotTable = keySpace + "." + table;
        this.idColumn = idColumn;
    }

    @Override
    public void put(String key, String value) {
        final String insert = String.format("INSERT INTO %s JSON '%s';", keyspaceDotTable, value);
        session.execute(insert);
    }

    @Override
    public void remove(String key) {
        final String delete = String.format("DELETE from %s WHERE id = '%s';", keyspaceDotTable, key);
        session.execute(delete);
    }

    @Override
    public String load(String key) throws Exception {
        final String select = String.format("SELECT JSON * from %s WHERE %s = '%s';", keyspaceDotTable, idColumn, key);
        final ResultSet execute = session.execute(select);
        return execute.one().getString(0);
    }

    @Override
    public String getName() {
        return "Json";
    }

    @Override
    protected boolean shouldCreateTable() {
        return false;
    }

    @Override
    protected void createTable() {
        throw new RuntimeException("Tables required for JSON caches "
            + "should be created prior to first usage.");
    }
}