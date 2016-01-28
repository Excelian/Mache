package com.excelian.mache.couchbase.builder;

import com.couchbase.client.java.cluster.BucketSettings;
import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.CouchbaseJsonCacheLoader;

/**
 * Proivisions Couchbase as storage with values in the mache expected as  Json
 * documents.
 */
public class CouchbaseJsonProvisioner implements StorageProvisioner {
    private final CouchbaseConnectionContext connectionContext;
    private final BucketSettings bucketSettings;
    private final String adminUser;
    private final String adminPassword;
    private final SchemaOptions schemaOptions;

    /**
     * Constructor.
     *
     * @param connectionContext - centrally managed resources
     * @param bucketSettings    - the couchbase bucket to use to store the data
     * @param adminUser         - admin username
     * @param adminPassword     - admin password
     * @param schemaOptions     - the schema policy
     */
    protected CouchbaseJsonProvisioner(CouchbaseConnectionContext connectionContext,
                                       BucketSettings bucketSettings,
                                       String adminUser, String adminPassword,
                                       SchemaOptions schemaOptions) {
        this.connectionContext = connectionContext;
        this.bucketSettings = bucketSettings;
        this.adminUser = adminUser;
        this.adminPassword = adminPassword;
        this.schemaOptions = schemaOptions;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> MacheLoader<K, V> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        if (keyType.equals(String.class) && valueType.equals(String.class)) {
            return (MacheLoader<K, V>) new CouchbaseJsonCacheLoader(bucketSettings,
                connectionContext, adminUser, adminPassword, schemaOptions);
        } else {
            throw new IllegalArgumentException("Only Cassandra Json Caches of type "
                + "<String, String> are supported.");
        }
    }
}
