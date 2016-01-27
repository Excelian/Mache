package com.excelian.mache.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.SchemaOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of the Mache CacheLoader for Couchbase Server. Utilises the
 * newer Spring Data Couchbase.
 *
 * @param <K> Cache key type.
 * @param <V> Cache value type.
 */
public class CouchbaseCacheLoader<K, V> extends AbstractCouchbaseCacheLoader<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseCacheLoader.class);
    private CouchbaseTemplate template;

    /**
     * @param keyType           The class type of the cache key.
     * @param valueType         The class type of the cache value.
     * @param bucketSettings    Bucket that will hold cached objects.
     * @param connectionContext Cluster connection.
     * @param adminUser         Administration user for Couchbase cluster.
     * @param adminPassword     Password for Administration user for Couchbase cluster.
     * @param schemaOptions     Determine whether to create/drop bucket.
     */
    public CouchbaseCacheLoader(Class<K> keyType, Class<V> valueType, BucketSettings bucketSettings,
                                ConnectionContext<Cluster> connectionContext, String adminUser,
                                String adminPassword, SchemaOptions schemaOptions) {
        super(keyType, valueType, bucketSettings, connectionContext, adminUser, adminPassword, schemaOptions);
    }

    @Override
    public void create() {
        super.create();
        template = new CouchbaseTemplate(manager.info(), bucket);
    }

    @Override
    public V load(K key) throws Exception {
        checkNotNull(key);
        return template.findById(key.toString(), valueType);
    }

    @Override
    public void put(K key, V value) {
        template.save(value);
    }

    @Override
    public void remove(K key) {
        template.remove(key);
    }

}

