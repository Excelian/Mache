package com.excelian.mache.couchbase.builder;

import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.CouchbaseCacheLoader;
import com.excelian.mache.couchbase.CouchbaseConfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link StorageProvisioner} implementation for Couchbase.
 */
public class CouchbaseProvisioner implements StorageProvisioner {

    @Override
    public String getStorage() {
        return "Couchbase";
    }

    @Override
    public <K, V> Mache<K, V> getCache(String keySpace, Class<V> valueType, SchemaOptions schemaOption,
                                       ClusterDetails clusterDetails, StorageServerDetails... serverDetails) {

        List<String> addresses = Arrays.stream(serverDetails)
                .map(StorageServerDetails::getAddress)
                .collect(Collectors.toList());

        CouchbaseConfig config = CouchbaseConfig.builder()
                .withServerAddresses(addresses)
                .withBucketName(keySpace)
                .build();

        return new MacheFactory().create(new CouchbaseCacheLoader<>(config));
    }
}
