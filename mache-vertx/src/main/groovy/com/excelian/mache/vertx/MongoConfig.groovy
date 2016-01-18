package com.excelian.mache.vertx

import com.excelian.mache.builder.NoMessagingProvisioner
import com.excelian.mache.core.SchemaOptions
import com.mongodb.ServerAddress

import static com.excelian.mache.guava.GuavaMacheProvisioner.guava
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;

public class MongoConfig {
    protected static String keySpace = "NoSQL_MacheClient_Test_" + new Date().toString();

    public static void main(String[] args) {
        MacheVertical vertical = new MacheVertical(
                guava(),
                mongodb()
                        .withSeeds(new ServerAddress("localhost", 27017))
                        .withDatabase(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build(),
                new NoMessagingProvisioner()
        );

        vertical.run();
    }
}