package com.excelian.mache.mongo;

import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.mongo.builder.MongoConnectionContext;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

public class MongoDBCacheLoader<K, V> implements MacheLoader<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBCacheLoader.class);
    private final List<MongoCredential> credentials;
    private final MongoClientOptions clientOptions;
    private MongoClient mongoClient;
    private Class<K> keyType;
    private Class<V> valueType;
    private MongoConnectionContext mongoConnectionContext;
    private SchemaOptions schemaOptions;
    private CollectionOptions collectionOptions;
    private String database;

    public MongoDBCacheLoader(Class<K> keyType, Class<V> valueType, MongoConnectionContext connectionContext,
                              List<MongoCredential> credentials, MongoClientOptions clientOptions,
                              String database, SchemaOptions schemaOptions, CollectionOptions collectionOptions) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.mongoConnectionContext = connectionContext;
        this.credentials = credentials;
        this.clientOptions = clientOptions;
        this.database = database;
        this.schemaOptions = schemaOptions;
        this.collectionOptions = collectionOptions;
        this.database = database.replace("-", "_").replace(" ", "_").replace(":", "_");
    }

    @Override
    public String getName() {
        return valueType.getSimpleName();
    }

    @Override
    public void create() {
        if (mongoClient == null) {
            synchronized (this) {
                if (mongoClient == null) {
                    mongoClient = connect();

                    if (!ops().collectionExists(valueType)) {
                        ops().createCollection(valueType, collectionOptions);
                    }
                }
            }
        }
    }

    @Override
    public void put(K key, V value) {
        LOG.trace("Saving to mongo key={}, newValue={}", key, value);
        ops().save(value);
    }

    @Override
    public void remove(K key) {
        String idField = ops().getConverter().getMappingContext()
                .getPersistentEntity(valueType).getIdProperty().getFieldName();
        ops().remove(new Query(Criteria.where(idField).is(key)), valueType);
    }

    @Override
    public V load(K key) {
        V value = ops().findById(key, valueType);
        LOG.trace("Loading from mongo by key {} - result {}", key, value);
        return value;
    }

    @Override
    public void close() {
        if (mongoClient != null) {
            synchronized (this) {
                if (mongoClient != null) {
                    if (schemaOptions.shouldDropSchema()) {
                        mongoClient.dropDatabase(database);
                        LOG.info("Dropped database {}", database);
                    }
                    mongoClient.close();
                    mongoClient = null;
                    mongoConnectionContext.close(this);
                }
            }
        }
    }

    private MongoOperations ops() {
        return new MongoTemplate(mongoClient, database);
    }

    private MongoClient connect() {
        return new MongoClient(mongoConnectionContext.getConnection(this), credentials, clientOptions);
    }

    @Override
    public String toString() {
        return "MongoDBCacheLoader{"
                + "credentials=" + credentials
                + ", clientOptions=" + clientOptions
                + ", mongoClient=" + mongoClient
                + ", keyType=" + keyType
                + ", valueType=" + valueType
                + ", mongoConnectionContext=" + mongoConnectionContext
                + ", schemaOptions=" + schemaOptions
                + ", collectionOptions=" + collectionOptions
                + ", database='" + database + '\''
                + '}';
    }
}
