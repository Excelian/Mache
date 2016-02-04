package com.excelian.mache.couchbase;

import com.couchbase.client.java.cluster.BucketSettings;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.builder.CouchbaseConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.couchbase.core.CouchbaseTemplate;

/**
 * An implementation of the Mache CacheLoader for Couchbase Server. Utilises the
 * newer Spring Data Couchbase.
 *
 * @param <K> Cache key type.
 * @param <V> Cache value type.
 */
public class CouchbaseCacheLoader<K, V> extends AbstractCouchbaseCacheLoader<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseCacheLoader.class);
    private final Class<V> valueType;
    private CouchbaseTemplate template;

    /**
     * @param valueType         The class type of the cache value.
     * @param bucketSettings    Bucket that will hold cached objects.
     * @param connectionContext Cluster connection.
     * @param adminUser         Administration user for Couchbase cluster.
     * @param adminPassword     Password for Administration user for Couchbase cluster.
     * @param schemaOptions     Determine whether to create/drop bucket.
     */
    public CouchbaseCacheLoader(Class<V> valueType, BucketSettings bucketSettings,
                                CouchbaseConnectionContext connectionContext, String adminUser,
                                String adminPassword, SchemaOptions schemaOptions) {
        super(bucketSettings, connectionContext, adminUser, adminPassword, schemaOptions);
        this.valueType = valueType;
    }

    @Override
    public void create() {
        super.create();
        template = new CouchbaseTemplate(manager.info(), bucket);
    }

    @Override
    public V load(K key) throws Exception {
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

