package com.excelian.mache.builder;

import com.excelian.mache.core.ExCache;
import com.excelian.mache.core.SchemaOptions;

/**
 * Created by jbowkett on 11/08/15.
 */
public interface StorageProvisioner {
  String getStorage();

  <K, V> ExCache<K, V> getCache(String keySpace, Class<V> valueType, SchemaOptions schemaOption, ClusterDetails clusterDetails, StorageServerDetails...serverDetails);


  public static class StorageServerDetails {
    private final String address;
    private final int port;

    public StorageServerDetails(String address, int port) {
      this.address = address;
      this.port = port;
    }

    public String getAddress() {
      return address;
    }

    public int getPort() {
      return port;
    }
  }

  public static class ClusterDetails {
    private final String name;

    public ClusterDetails(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static class IgnoredClusterDetails extends ClusterDetails {
    public IgnoredClusterDetails() {
      super("NoCluster");
    }
  }
}
