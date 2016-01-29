package com.excelian.mache.mongo;

import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.mongo.builder.MongoConnectionContext;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Abstract base class for common functionality for the Mongo DB cache loaders.
 *
 * @param <K> the type of the keys
 * @param <V> the type of the values
 */
public abstract class AbstractMongoDBCacheLoader<K, V> implements MacheLoader<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMongoDBCacheLoader.class);

    protected final List<MongoCredential> credentials;
    protected final MongoClientOptions clientOptions;
    protected MongoClient mongoClient;
    protected Class<K> keyType;
    protected Class<V> valueType;
    protected MongoConnectionContext mongoConnectionContext;
    protected SchemaOptions schemaOptions;
    protected String databaseName;

    /**
     * Constructor.
     *
     * @param connectionContext the connection context containing shared managed
     *                          resources
     * @param clientOptions     the mongo DB client options
     * @param keyType           the type of the keys in mache
     * @param schemaOptions     the policy on schema creation
     * @param valueType         the type of the values in mache
     * @param credentials       the logon credentials to use
     * @param databaseName      the db name
     */
    public AbstractMongoDBCacheLoader(MongoConnectionContext connectionContext,
                                      MongoClientOptions clientOptions,
                                      Class<K> keyType, SchemaOptions schemaOptions,
                                      Class<V> valueType, List<MongoCredential> credentials,
                                      String databaseName) {
        this.mongoConnectionContext = connectionContext;
        this.clientOptions = clientOptions;
        this.keyType = keyType;
        this.schemaOptions = schemaOptions;
        this.valueType = valueType;
        this.credentials = credentials;
        this.databaseName = databaseName.replace("-", "_").replace(" ", "_").replace(":", "_");
    }

    @Override
    public void create() {
        if (mongoClient == null) {
            synchronized (this) {
                if (mongoClient == null) {
                    mongoClient = connect();

                    if (shouldCreateCollection()) {
                        createCollection();
                    }
                }
            }
        }
    }

    protected abstract void createCollection();

    protected abstract boolean shouldCreateCollection();


    @Override
    public void close() {
        if (mongoClient != null) {
            synchronized (this) {
                if (mongoClient != null) {
                    if (schemaOptions.shouldDropSchema()) {
                        mongoClient.dropDatabase(databaseName);
                        LOG.info("Dropped database {}", databaseName);
                    }
                    mongoClient.close();
                    mongoClient = null;
                    mongoConnectionContext.close(this);
                }
            }
        }
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
            + ", database='" + databaseName + '\''
            + '}';
    }

    private MongoClient connect() {
        return new MongoClient(mongoConnectionContext.getConnection(this), credentials, clientOptions);
    }
}
