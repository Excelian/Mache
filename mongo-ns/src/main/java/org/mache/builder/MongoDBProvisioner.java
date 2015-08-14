package org.mache.builder;

import com.mongodb.ServerAddress;
import org.mache.CacheThingFactory;
import org.mache.ExCache;
import org.mache.MongoDBCacheLoader;
import org.mache.SchemaOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * Created by jbowkett on 11/08/15.
 */
public class MongoDBProvisioner implements StorageProvisioner {
  @Override
  public String getStorage() {
    return "Mongo";
  }

  @Override
  public <K, V> ExCache<K, V> getCache(String keySpace, Class<V> valueType, SchemaOptions schemaOption, ClusterDetails clusterDetails, StorageServerDetails... serverDetails) {
    final MongoDBCacheLoader<K, V> cacheLoader = getCacheLoader(valueType, keySpace, schemaOption, serverDetails);
    final CacheThingFactory cacheThingFactory = new CacheThingFactory();
    return cacheThingFactory.create(cacheLoader);
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
