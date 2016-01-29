package com.excelian.mache.mongo.builder;

import com.excelian.mache.builder.StorageProvisioner;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.excelian.mache.mongo.MongoDBJsonCacheLoader;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.data.mongodb.core.CollectionOptions;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.stream;

/**
 * {@link StorageProvisioner} implementation for Mongo DB.
 */
public class MongoDBProvisioner implements StorageProvisioner {

    private final MongoDBConnectionContext connectionContext;
    private final List<MongoCredential> mongoCredentials;
    private final MongoClientOptions clientOptions;
    private final String database;
    private final SchemaOptions schemaOptions;
    private final CollectionOptions collectionOptions;

    /**
     * Constructor.
     *
     * @param connectionContext - connectionContext
     * @param credentials       - credentials
     * @param clientOptions     - clientOptions
     * @param database          - database
     * @param schemaOptions     - schemaOptions
     * @param collectionOptions - collectionOptions
     */
    private MongoDBProvisioner(MongoDBConnectionContext connectionContext, List<MongoCredential> credentials,
                               MongoClientOptions clientOptions, String database, SchemaOptions schemaOptions,
                               CollectionOptions collectionOptions) {

        this.connectionContext = connectionContext;
        this.mongoCredentials = credentials;
        this.clientOptions = clientOptions;
        this.database = database;
        this.schemaOptions = schemaOptions;
        this.collectionOptions = collectionOptions;
    }

    /**
     * Gets default mongodb config.
     *
     * @return the start of the builder for mongodb
     */
    public static SeedsListBuilder mongodb() {
        return seeds -> database -> {
            final MongoDBConnectionContext mongoDBConnectionContext = MongoDBConnectionContext.getInstance(seeds);
            return new MongoDBProvisionerBuilder(mongoDBConnectionContext, database);
        };
    }


    /**
     * Creates a mongo connection context from the server addresses.
     *
     * @param seeds the mongo servers
     * @return the connection context
     */
    public static MongoDBConnectionContext mongoConnectionContext(ServerAddress... seeds) {
        return MongoDBConnectionContext.getInstance(seeds);
    }

    @Override
    public <K, V> MacheLoader<K, V> getCacheLoader(Class<K> keyType, Class<V> valueType) {
        // TODO this is the wrong approach but gets tests running for now
        if (valueType == String.class) {
            return (MacheLoader<K, V>) new MongoDBJsonCacheLoader(connectionContext,
                mongoCredentials, clientOptions, database, schemaOptions, "SLIMEª");
        } else {
            return new MongoDBCacheLoader<>(keyType, valueType, connectionContext, mongoCredentials,
                clientOptions, database, schemaOptions, collectionOptions);
        }
    }

    /**
     * Forces seeds to be provided.
     */
    public interface SeedsListBuilder {
        DatabaseNameBuilder withSeeds(ServerAddress... seeds);
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
        private final MongoDBConnectionContext connectionContext;
        private final String database;
        private List<MongoCredential> mongoCredentials = Collections.emptyList();
        private MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
        private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;
        private CollectionOptions collectionOptions = null;

        private MongoDBProvisionerBuilder(MongoDBConnectionContext connectionContext, String database) {
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
            return new MongoDBProvisioner(connectionContext, mongoCredentials,
                mongoClientOptions, database, schemaOptions, collectionOptions);
        }
    }
}
