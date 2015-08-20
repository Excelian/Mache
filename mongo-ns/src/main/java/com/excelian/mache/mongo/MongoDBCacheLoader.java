package com.excelian.mache.mongo;

import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

/**
 * CacheLoader to bind Cassandra API onto the GuavaCache
 * TODO: Replication class and factor need to be configurable.
 */
public class MongoDBCacheLoader<K, V> extends AbstractCacheLoader<K, V, Mongo> {
    private static final Logger LOG = LoggerFactory.getLogger(MongoDBCacheLoader.class);

    private Mongo mongoClient;
    private List<ServerAddress> hosts;
    private SchemaOptions schemaOptions;
    private String keySpace;

    private boolean isTableCreated = false;

    private Class<V> clazz;

    public MongoDBCacheLoader(Class<V> clazz, List<ServerAddress> hosts, SchemaOptions schemaOptions, String keySpace) {
        this.hosts = hosts;
        this.schemaOptions = schemaOptions;
        this.keySpace = keySpace;
        this.keySpace = keySpace.replace("-", "_").replace(" ", "_").replace(":", "_");
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return clazz.getSimpleName();
    }

    public void create(String name, Object k) {
        if (schemaOptions.shouldCreateSchema() && mongoClient == null) {
            synchronized (this) {
                if (mongoClient == null) {
                    try {
                        this.mongoClient = connect(hosts);

                        if (schemaOptions.shouldCreateSchema()) {
                            createKeySpace();
                        }
                        createTable();
                    } catch (Throwable t) {
                        LOG.error("Failed to create: {}", t);

                    }
                }
            }
        } else {
            this.mongoClient = connect(hosts);
        }
    }

    private void createKeySpace() {
        // implicit in connect?
    }

    private void createTable() {
        if (!isTableCreated) {
            isTableCreated = true;
            if (!ops().collectionExists(clazz)) {
                ops().createCollection(clazz);
            }
        }
    }

    public void put(K key, V value) {
        LOG.trace("Saving to mongo key={}, newValue={}", key, value);
        ops().save(value);
    }

    public void remove(K key) {
        // unfortunately cant delete by id
        V byId = ops().findById(key, clazz);
        ops().remove(byId);
    }

    @Override
    public V load(K key) throws Exception {
        V value = ops().findById(key, clazz);
        LOG.trace("Loading from mongo by key {} - result {}", key, value);
        return value;
    }

    @Override
    public void close() {
        if (mongoClient != null) {
            if (schemaOptions.shouldDropSchema()) {
                mongoClient.dropDatabase(keySpace);
                LOG.info("Dropped database {}", keySpace);
            }
            mongoClient.close();
            mongoClient = null;
        }
    }

    @Override
    public Mongo getDriverSession() {
        if (mongoClient == null) {
            throw new IllegalStateException("Session has not been created - read/write to cache first");
        }
        return mongoClient;
    }

    private MongoOperations ops() {
        return new MongoTemplate(mongoClient, keySpace);
    }

    private Mongo connect(List<ServerAddress> hosts) {
        return new MongoClient(hosts);
    }
}
