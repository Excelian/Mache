package com.excelian.mache.vertx

import com.excelian.mache.core.SchemaOptions
import com.mongodb.ServerAddress

import static com.excelian.mache.builder.MacheBuilder.mache
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;

/**
 * Run the REST service using a Mongo storage provider, the maps created will not be evicted after any period
 * of time.
 */
public class MongoConfig {
    protected static String keySpace = "NoSQL_MacheClient_Test_" + new Date().toString();

    public static void main(String[] args) {
        MacheRestService restService = new MacheRestService();

        restService.runAsync({ context ->
            new RestManagedMache(
                    mache(String.class, String.class)
                            .cachedBy(guava())
                            .storedIn(
                            mongodb()
                                    .withSeeds(new ServerAddress("localhost", 27017))
                                    .withDatabase(keySpace)
                                    .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                                    .asJsonDocuments()
                                    .inCollection("test"))
                            .withNoMessaging()
                            .macheUp(), 0)
        });
    }
}