package com.excelian.mache.mongo.builder;

import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.builder.storage.StorageProvisioner;
import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.data.mongodb.core.CollectionOptions;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * {@link StorageProvisioner} implementation for Mongo DB.
 */
public class MongoDBProvisioner implements StorageProvisioner {

    private final ConnectionContext<List<ServerAddress>> connectionContext;
    private final List<MongoCredential> mongoCredentials;
    private final MongoClientOptions clientOptions;
    private final String database;
    private final SchemaOptions schemaOptions;
    private final CollectionOptions collectionOptions;

    private MongoDBProvisioner(ConnectionContext<List<ServerAddress>> connectionContext, List<MongoCredential> credentials,
                               MongoClientOptions clientOptions, String database, SchemaOptions schemaOptions,
                               CollectionOptions collectionOptions) {

        this.connectionContext = connectionContext;
        this.mongoCredentials = credentials;
        this.clientOptions = clientOptions;
        this.database = database;
        this.schemaOptions = schemaOptions;
        this.collectionOptions = collectionOptions;
    }

    public static ConnectionContextBuilder mongodb() {
        return connectionContext -> database -> new MongoDBProvisionerBuilder(connectionContext, database);
    }

    public static ConnectionContext<List<ServerAddress>> mongoConnectionContext(ServerAddress... seeds) {
        return new ConnectionContext<List<ServerAddress>>() {
            @Override
            public List<ServerAddress> getConnection() {
                return stream(seeds).collect(toList());
            }

            @Override
            public void close() throws Exception {
                return;
            }
        };
    }

    @Override
    public <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType) {
        final MacheFactory macheFactory = new MacheFactory();
        return macheFactory.create(getCacheLoader(keyType, valueType));
    }

    @Override
    public <K, V> AbstractCacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        return new MongoDBCacheLoader<>(keyType, valueType, connectionContext, mongoCredentials, clientOptions, database,
                schemaOptions, collectionOptions);
    }

    /**
     * Forces seeds to be provided.
     */
    public interface ConnectionContextBuilder {
        DatabaseNameBuilder withConnectionContext(ConnectionContext<List<ServerAddress>> context);
    }


    /**
     * Forces database name to be provided.
     */
    public interface DatabaseNameBuilder {
        MongoDBProvisionerBuilder withDatabase(String database);
    }

    /**
     * A builder with defaults for a Mongo DB cluster.
     */
    public static class MongoDBProvisionerBuilder {
        private final ConnectionContext<List<ServerAddress>> connectionContext;
        private final String database;
        private List<MongoCredential> mongoCredentials = Collections.emptyList();
        private MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;
        private CollectionOptions collectionOptions = null;

        private MongoDBProvisionerBuilder(ConnectionContext<List<ServerAddress>> connectionContext, String database) {
            this.connectionContext = connectionContext;
            this.database = database;
        }

        public MongoDBProvisionerBuilder withMongoCredentials(List<MongoCredential> mongoCredentials) {
            this.mongoCredentials = mongoCredentials;
            return this;
        }

        public MongoDBProvisionerBuilder withMongoClientOptions(MongoClientOptions mongoClientOptions) {
            this.mongoClientOptions = mongoClientOptions;
            return this;
        }

        public MongoDBProvisionerBuilder withSchemaOptions(SchemaOptions schemaOptions) {
            this.schemaOptions = schemaOptions;
            return this;
        }

        public MongoDBProvisionerBuilder withCollectionOptions(CollectionOptions collectionOptions) {
            this.collectionOptions = collectionOptions;
            return this;

        }

        public MongoDBProvisioner build() {
            return new MongoDBProvisioner(connectionContext, mongoCredentials, mongoClientOptions, database, schemaOptions,
                    collectionOptions);
        }
    }
}
