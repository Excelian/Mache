package org.mache;

import com.datastax.driver.core.*;
import org.springframework.cassandra.core.cql.CqlIdentifier;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.*;

import java.util.*;

/**
 * CacheLoader to bind Cassandra API onto the GuavaCache
 *
 * Created by neil.avery on 29/05/2015.
 * @implNote : Replication class and factor need to be configurable.
 */
public class CassandraCacheLoader<K,V> extends AbstractCacheLoader<K,V, Session> {

    private static final int REPLICATION_FACTOR = 1;
    private static final String REPLICATION_CLASS = "SimpleStrategy";

    final private Cluster cluster;
    private Session session;
    private boolean isSchemaCreate;
    final private String keySpace;

    private boolean isTableCreated = false;

    final private Class<V> clazz;

    public CassandraCacheLoader(Class<V> clazz, Cluster cluster, boolean isSchemaCreate, String keySpace) {
        this.cluster = cluster;
        this.cluster.getConfiguration().getQueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
        this.isSchemaCreate = isSchemaCreate;
        this.keySpace = keySpace.replace("-","_").replace(" ","_").replace(":","_");
        this.clazz = clazz;
    }

    public void create(String name, K k) {
        if (isSchemaCreate && session == null) {
            synchronized (this) {
                if (isSchemaCreate && session == null) {
                    try {
                        isSchemaCreate = false;
                        session = cluster.connect();
                        createKeySpace();
                        createTable();
                    } catch (Throwable t) {
                        System.err.println("Keyspace:" + keySpace);
                        t.printStackTrace();
                        System.err.println("Failed to create:" + t.getMessage());
                    }
                }
            }
        } else {
            session = cluster.connect(keySpace);
        }
    }

    private void createKeySpace() {
        session.execute(String.format("CREATE KEYSPACE IF NOT EXISTS %s  WITH REPLICATION = {'class':'%s', 'replication_factor':%d}; ", keySpace, REPLICATION_CLASS, REPLICATION_FACTOR));
        session.execute(String.format("USE %s ", keySpace));
    }

    void createTable() {
        if (!isTableCreated) {
            isTableCreated = true;
            CassandraAdminTemplate adminTemplate = new CassandraAdminTemplate(session, new MappingCassandraConverter());
            Map<String, Object> optionsByName = null;
            adminTemplate.createTable(true, new CqlIdentifier(getTableName()), clazz, optionsByName);

        }
    }

    public void put(K k, V v) {
        ops().insert(v);
    }

    public void remove(K k) {
        ops().deleteById(clazz, k);
    }

    @Override
    public V load(K key) throws Exception {
        Object o = ops().selectOneById(clazz, key);
        return (V) o;
    }

    @Override
    public void close() {
        if (session != null && !session.isClosed()) session.close();
    }

    @Override
    public String getName() {
        return clazz.getSimpleName();
    }

    @Override
    public Session getDriverSession() {
        if (session == null) throw  new IllegalStateException("Session has not been created - read/write to cache first");
        return session;
    }

    private CassandraOperations ops() {
        return new CassandraTemplate(session);
    }

    public static Cluster connect(String contactPoint, String clusterName, int port) {
        Cluster cluster = Cluster.builder().addContactPoint(contactPoint).withPort(port).withClusterName(clusterName).build();
        Metadata metadata = cluster.getMetadata();
        return cluster;
    }

    private String getTableName() {
        String value = clazz.getAnnotation(Table.class).value();
        if (value.length() == 0) value = clazz.getSimpleName();
        return value;
    }
}
