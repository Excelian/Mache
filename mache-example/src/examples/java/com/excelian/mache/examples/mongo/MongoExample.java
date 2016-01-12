package com.excelian.mache.examples.mongo;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;
import com.mongodb.ServerAddress;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;
import static com.excelian.mache.guava.builder.GuavaProvisioner.guava;

/**
 * A factory for a Mongo backed {@link Example}.
 */
public class MongoExample implements Example<MongoAnnotatedMessage> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Override
    public Mache<String, MongoAnnotatedMessage> exampleCache() throws Exception {
        final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
        return mache(String.class, MongoAnnotatedMessage.class)
                .cachedBy(guava())
                .backedBy(mongodb()
                        .withSeeds(new ServerAddress("10.28.1.140", 9042))
                        .withDatabase(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build())
                .withNoMessaging()
                .macheUp();
    }

}

