package com.excelian.mache.mongo.builder;

import com.excelian.mache.builder.StorageProvisioner;
import com.mongodb.ServerAddress;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.Mache;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.excelian.mache.core.SchemaOptions;

import java.util.Arrays;


/**
 * Created by jbowkett on 11/08/15.
 */
public class MongoDBProvisioner implements StorageProvisioner {
  @Override
  public String getStorage() {
    return "Mongo";
  }

  @Override
  public <K, V> Mache<K, V> getCache(String keySpace, Class<V> valueType, SchemaOptions schemaOption, ClusterDetails clusterDetails, StorageServerDetails... serverDetails) {
    final MongoDBCacheLoader<K, V> cacheLoader = getCacheLoader(valueType, keySpace, schemaOption, serverDetails);
    final MacheFactory macheFactory = new MacheFactory();
    return macheFactory.create(cacheLoader);
  }

  private <K, V> MongoDBCacheLoader<K, V> getCacheLoader(Class<V> valueType, String keySpace, SchemaOptions schemaOption, StorageServerDetails[] serverDetails) {
    final ServerAddress[] serverAddresses = Arrays.stream(serverDetails)
        .map(ssd -> new ServerAddress(ssd.getAddress(), ssd.getPort()))
        .toArray(ServerAddress[]::new);

    return new MongoDBCacheLoader<>(
        valueType,
        Arrays.asList(serverAddresses),
        schemaOption,
        keySpace);
  }
}
