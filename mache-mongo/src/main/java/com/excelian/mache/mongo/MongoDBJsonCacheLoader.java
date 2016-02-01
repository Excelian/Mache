package com.excelian.mache.mongo;

import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.mongo.builder.MongoDBConnectionContext;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Loads a MongoDB cache with Json documents.
 */
public class MongoDBJsonCacheLoader extends AbstractMongoDBCacheLoader<String, String> {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBJsonCacheLoader.class);
    private static final UpdateOptions UPSERT = getUpsert();
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private final String collectionName;

    /**
     * Constructor.
     * @param mongoDBConnectionContext shared mongo DB resources
     * @param credentials              logon credentials
     * @param clientOptions            mongo client options
     * @param database                 database name
     * @param schemaOptions            schema creation policy
     * @param collectionName           collection to persist to and from
     */
    public MongoDBJsonCacheLoader(MongoDBConnectionContext mongoDBConnectionContext,
                                  List<MongoCredential> credentials,
                                  MongoClientOptions clientOptions,
                                  String database, SchemaOptions schemaOptions,
                                  String collectionName) {
        super(mongoDBConnectionContext, clientOptions, String.class,
            schemaOptions, String.class, credentials, database);
        this.collectionName = collectionName;
    }

    @Override
    public void create() {
        super.create();
        setDatabaseIfNotAlready();
        this.collection = database.getCollection(this.collectionName);
    }

    @Override
    public String getName() {
        return String.class.getSimpleName();
    }


    @Override
    public void put(String key, String value) {
        LOG.trace("Saving to mongo key={}, newValue={}", key, value);
        final Document idFindQuery = new Document("_id", key);
        final Document payload = Document.parse(value);
        collection.replaceOne(idFindQuery, payload, UPSERT);
    }

    @Override
    public void remove(String key) {
        LOG.trace("Removing from mongo key={}", key);
        BasicDBObject query = new BasicDBObject();
        query.put("_id", key);
        collection.deleteOne(query);
    }


    @Override
    public String load(String key) {
        String value = null;
        final FindIterable<Document> results = collection.find(eq("_id", key));
        for (Document document : results) {
            value = document.toJson();
        }
        return value;
    }

    @Override
    protected void createCollection() {
        setDatabaseIfNotAlready();
        this.database.createCollection(this.collectionName);
    }

    @Override
    protected boolean shouldCreateCollection() {
        setDatabaseIfNotAlready();
        return database.getCollection(this.collectionName) == null;
    }

    private static UpdateOptions getUpsert() {
        final UpdateOptions options = new UpdateOptions();
        options.upsert(true);
        return options;
    }

    private void setDatabaseIfNotAlready() {
        if (this.database == null) {
            this.database = mongoClient.getDatabase(this.databaseName);
        }
    }
}
