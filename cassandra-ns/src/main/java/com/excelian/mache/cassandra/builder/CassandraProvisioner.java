package com.excelian.mache.cassandra.builder;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.CacheThingFactory;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.excelian.mache.ExCache;
import com.excelian.mache.SchemaOptions;

/**
 * Created by jbowkett on 11/08/15.
 */
public class CassandraProvisioner implements StorageProvisioner {
  @Override
  public String getStorage() {
    return "Cassandra";
  }

  @Override
  public <K, V> ExCache<K, V> getCache(String keySpace, Class<V> valueType, SchemaOptions schemaOption, ClusterDetails clusterDetails, StorageServerDetails...serverDetails){
    final Cluster cluster = getCluster(serverDetails[0], clusterDetails);
    final CassandraCacheLoader<K, V> cacheLoader = getCacheLoader(keySpace, cluster, valueType, schemaOption);

    final CacheThingFactory cacheThingFactory = new CacheThingFactory();
    return cacheThingFactory.create(cacheLoader);
  }

  private Cluster getCluster(StorageServerDetails server, ClusterDetails clusterDetails) {
    System.out.println("Connecting to Cassandra cluster...");
    final Cluster cluster = CassandraCacheLoader.connect(
        server.getAddress(), clusterDetails.getName(), server.getPort());
    System.out.println("Connected.");
    return cluster;
  }


  private <K,V> CassandraCacheLoader<K, V> getCacheLoader(String keySpace, Cluster cluster, Class<V> valueTypes, SchemaOptions createanddropschema){
    System.out.println("Creating cache loader with keyspace:["+keySpace+"]");
    final CassandraCacheLoader<K, V> cacheLoader = new CassandraCacheLoader<>(
        valueTypes, cluster, createanddropschema, keySpace);
    System.out.println("CacheLoader created.");
    return cacheLoader;
  }
}
