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

/**
 * Cacheloader to connect to Mongo for its data.
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public class MongoDBCacheLoader<K, V> extends AbstractMongoDBCacheLoader<K, V> {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBCacheLoader.class);
    private final CollectionOptions collectionOptions;

    /**
     * Constructor.
     * @param keyType - keyType
     * @param valueType - valueType
     * @param connectionContext - connectionContext
     * @param credentials - credentials
     * @param clientOptions - clientOptions
     * @param database - database
     * @param schemaOptions - schemaOptions
     * @param collectionOptions - collectionOptions
     */
    public MongoDBCacheLoader(Class<K> keyType, Class<V> valueType,
                              MongoConnectionContext connectionContext,
                              List<MongoCredential> credentials,
                              MongoClientOptions clientOptions,
                              String database, SchemaOptions schemaOptions,
                              CollectionOptions collectionOptions) {
        super(connectionContext, clientOptions, keyType, schemaOptions, valueType,
            credentials, database);
        this.collectionOptions = collectionOptions;
    }

    @Override
    public String getName() {
        return valueType.getSimpleName();
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

    private MongoOperations ops() {
        return new MongoTemplate(mongoClient, databaseName);
    }

    @Override
    protected void createCollection() {
        ops().createCollection(valueType, collectionOptions);
    }

    @Override
    protected boolean shouldCreateCollection() {
        return this.schemaOptions.shouldCreateSchema() && !ops().collectionExists(valueType);
    }
}
