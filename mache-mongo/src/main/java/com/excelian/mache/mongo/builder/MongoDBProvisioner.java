package com.excelian.mache.mongo.builder;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.List;

import org.springframework.data.mongodb.core.CollectionOptions;

import com.excelian.mache.builder.storage.StorageProvisioner;
import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

/**
 * {@link StorageProvisioner} implementation for Mongo DB.
 */
public class MongoDBProvisioner implements StorageProvisioner {

	private final List<ServerAddress> seeds;
	private final List<MongoCredential> mongoCredentials;
	private final MongoClientOptions clientOptions;
	private final String database;
	private final SchemaOptions schemaOptions;
	private final CollectionOptions collectionOptions;

	private MongoDBProvisioner(List<ServerAddress> seeds, List<MongoCredential> credentials,
			MongoClientOptions clientOptions, String database, SchemaOptions schemaOptions,
			CollectionOptions collectionOptions) {
		this.seeds = seeds;
		this.mongoCredentials = credentials;
		this.clientOptions = clientOptions;
		this.database = database;
		this.schemaOptions = schemaOptions;
		this.collectionOptions = collectionOptions;
	}

	public static SeedsListBuilder mongodb() {
		return seeds -> database -> new MongoDBProvisionerBuilder(stream(seeds).collect(toList()), database);
	}

	@Override
	public <K, V> Mache<K, V> getCache(Class<K> keyType, Class<V> valueType) {
		final MacheFactory macheFactory = new MacheFactory();
		return macheFactory.create(getCacheLoader(keyType, valueType));
	}

	@Override
	public <K, V> AbstractCacheLoader<K, V, ?> getCacheLoader(Class<K> keyType, Class<V> valueType) {
		return new MongoDBCacheLoader<>(keyType, valueType, seeds, mongoCredentials, clientOptions, database,
				schemaOptions, collectionOptions);
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
		private final List<ServerAddress> seeds;
		private final String database;
		private List<MongoCredential> mongoCredentials = Collections.emptyList();
		private MongoClientOptions mongoClientOptions = MongoClientOptions.builder().build();
		private SchemaOptions schemaOptions = SchemaOptions.USE_EXISTING_SCHEMA;
		private CollectionOptions collectionOptions = null;

		private MongoDBProvisionerBuilder(List<ServerAddress> seeds, String database) {
			this.seeds = seeds;
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
			return new MongoDBProvisioner(seeds, mongoCredentials, mongoClientOptions, database, schemaOptions,
					collectionOptions);
		}
	}
}
