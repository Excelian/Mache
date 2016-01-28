package com.excelian.mache.couchbase;

import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.builder.CouchbaseConnectionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An implementation of the Mache CacheLoader for Couchbase Server. Utilises the
 * newer Spring Data Couchbase.
 */
public class CouchbaseJsonCacheLoader extends AbstractCouchbaseCacheLoader<String, String> {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseJsonCacheLoader.class);

    /**
     * @param bucketSettings    Bucket that will hold cached objects.
     * @param connectionContext Cluster connection.
     * @param adminUser         Administration user for Couchbase cluster.
     * @param adminPassword     Password for Administration user for Couchbase cluster.
     * @param schemaOptions     Determine whether to create/drop bucket.
     */
    public CouchbaseJsonCacheLoader(BucketSettings bucketSettings,
                                    CouchbaseConnectionContext connectionContext, String adminUser,
                                    String adminPassword, SchemaOptions schemaOptions) {
        super(bucketSettings, connectionContext, adminUser, adminPassword, schemaOptions);
    }

    @Override
    public String load(String key) throws Exception {
        final JsonDocument jsonDocument = bucket.get(key);
        if (jsonDocument == null) {
            return null;
        }
        return jsonDocument.content().toString();
    }

    @Override
    public void put(String key, String value) {
        final JsonObject jsonObject = JsonObject.fromJson(value);
        final JsonDocument document = JsonDocument.create(key, jsonObject);
        bucket.upsert(document);
    }

    @Override
    public void remove(String key) {
        bucket.remove(key);
    }
}

