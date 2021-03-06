package com.excelian.mache.mongo.builder;

import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.mongo.MongoDBJsonCacheLoader;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import org.springframework.data.mongodb.core.CollectionOptions;

import java.util.List;

/**
 * Provisions a Mongo DB cache loader that stores string values as Json.
 */
public class MongoDBJsonProvisioner implements StorageProvisioner<String, String> {
    private final MongoDBConnectionContext connectionContext;
    private final List<MongoCredential> mongoCredentials;
    private final MongoClientOptions mongoClientOptions;
    private final String database;
    private final SchemaOptions schemaOptions;
    private final CollectionOptions collectionOptions;
    private final String collection;

    /**
     * Constructor.
     *
     * @param connectionContext  - connectionContext
     * @param mongoCredentials   - mongoCredentials
     * @param mongoClientOptions - mongoClientOptions
     * @param database           - database
     * @param schemaOptions      - schemaOptions
     * @param collectionOptions  - collectionOptions
     * @param collection         - collection
     */
    public MongoDBJsonProvisioner(MongoDBConnectionContext connectionContext,
                                  List<MongoCredential> mongoCredentials,
                                  MongoClientOptions mongoClientOptions,
                                  String database, SchemaOptions schemaOptions,
                                  CollectionOptions collectionOptions, String collection) {
        this.connectionContext = connectionContext;
        this.mongoCredentials = mongoCredentials;
        this.mongoClientOptions = mongoClientOptions;
        this.database = database;
        this.schemaOptions = schemaOptions;
        this.collectionOptions = collectionOptions;
        this.collection = collection;
    }

    @Override
    public MacheLoader<String, String> getCacheLoader(Class<String> keyType, Class<String> valueType) {
        return new MongoDBJsonCacheLoader(connectionContext, mongoCredentials, mongoClientOptions,
                database, schemaOptions, collection);
    }
}
