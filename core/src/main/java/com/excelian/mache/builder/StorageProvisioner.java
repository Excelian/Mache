package com.excelian.mache.builder;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;

/**
 * Created by jbowkett on 11/08/15.
 */
public interface StorageProvisioner {
    String getStorage();

    <K, V> Mache<K, V> getCache(String keySpace, Class<V> valueType, SchemaOptions schemaOption, ClusterDetails clusterDetails, StorageServerDetails... serverDetails);


    class StorageServerDetails {
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

    class ClusterDetails {
        private final String name;

        public ClusterDetails(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    class IgnoredClusterDetails extends ClusterDetails {
        public IgnoredClusterDetails() {
            super("NoCluster");
        }
    }
}
