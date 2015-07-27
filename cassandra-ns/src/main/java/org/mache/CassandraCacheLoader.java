package org.mache;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import org.springframework.cassandra.core.cql.CqlIdentifier;
import org.springframework.data.cassandra.convert.MappingCassandraConverter;
import org.springframework.data.cassandra.core.CassandraAdminTemplate;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.mapping.*;

import java.util.*;

/**
 * CacheLoader to bind Cassandra API onto the GuavaCache
 * <p>
 * Created by neil.avery on 29/05/2015.
 *
 * @implNote : Replication class and factor need to be configurable.
 */
public class CassandraCacheLoader<K, V> extends AbstractCacheLoader<K, V, Session> {

  private static final int REPLICATION_FACTOR = 1;
  private static final String REPLICATION_CLASS = "SimpleStrategy";

  final private Cluster cluster;
  private Session session;
  private SchemaOptions schemaOption;
  final private String keySpace;

  private boolean isTableCreated = false;

  final private Class<V> clazz;

  public CassandraCacheLoader(Class<V> clazz, Cluster cluster, SchemaOptions schemaOption, String keySpace) {
    this.cluster = cluster;

    this.cluster.getConfiguration().getQueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
    this.schemaOption = schemaOption;
    this.keySpace = keySpace.replace("-", "_").replace(" ", "_").replace(":", "_");
    this.clazz = clazz;
  }

  public void create(String ignored1, K ignored2) {
    if (schemaOption.ShouldCreateSchema() && session == null) {
      synchronized (this) {
        if (session == null) {
          try {
            session = cluster.connect();
            if (schemaOption.ShouldCreateSchema()) {
              createKeySpace();
            }
            createTable();
          }
          catch (Throwable t) {
            System.err.println("Keyspace:" + keySpace);
            t.printStackTrace();
            System.err.println("Failed to create:" + t.getMessage());
          }
        }
      }
    }
    else {
      session = cluster.connect(keySpace);
    }
  }

  private void createKeySpace() {
    session.execute(String.format("CREATE KEYSPACE IF NOT EXISTS %s  WITH REPLICATION = {'class':'%s', 'replication_factor':%d}; ", keySpace, REPLICATION_CLASS, REPLICATION_FACTOR));
    session.execute(String.format("USE %s ", keySpace));
    System.out.println("Created keyspace " + keySpace);
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
    if (session != null && !session.isClosed()) {
      if (schemaOption.ShouldDropSchema()) {
        try {
          session.execute(String.format("DROP KEYSPACE %s; ", keySpace));
          System.out.println("Dropped keyspace" + keySpace);
        }
        catch (Throwable t) {
          t.printStackTrace();
          System.err.println("Failed to drop keyspace :" + keySpace + ". err=" + t.getMessage());
        }
      }
      session.close();
    }
  }

  @Override
  public String getName() {
    return clazz.getSimpleName();
  }

  @Override
  public Session getDriverSession() {
    if (session == null)
      throw new IllegalStateException("Session has not been created - read/write to cache first");
    return session;
  }

  private CassandraOperations ops() {
    return new CassandraTemplate(session);
  }

  public static Cluster connect(String contactPoint, String clusterName, int port) {
    Cluster cluster = Cluster.builder()
        .addContactPoint(contactPoint)
        .withPort(port)
        .withClusterName(clusterName)
        .withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy())) // go to the node with data
        .build();
    Metadata metadata = cluster.getMetadata();
    return cluster;
  }

  private String getTableName() {
    final Table annotation = getAnnotationOrThrow();
    final String value = annotation.value();
    if (value.length() == 0){
      return clazz.getSimpleName();
    }
    return value;
  }

  private Table getAnnotationOrThrow() {
    final Table annotation = clazz.getAnnotation(Table.class);
    if(annotation == null){
      throw new RuntimeException("Stored class ["+clazz.getSimpleName()+"] must specify Table annotation.");
    }
    return annotation;
  }
}
