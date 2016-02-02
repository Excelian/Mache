package com.excelian.mache.examples.mongo;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;
import com.mongodb.ServerAddress;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;

/**
 * A factory for a Mongo backed {@link Example}.
 */
public class MongoExample implements Example<MongoAnnotatedMessage, MongoAnnotatedMessage> {

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private String serverIpAddress;

    public MongoExample(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    @Override
    public Mache<String, MongoAnnotatedMessage> exampleCache() throws Exception {
        final String keySpace = "NoSQL_MacheClient_Test_" + dateFormat.format(new Date());
        return mache(String.class, MongoAnnotatedMessage.class)
                .cachedBy(guava())
                .storedIn(mongodb()
                    .withSeeds(new ServerAddress(serverIpAddress, 27017))
                        .withDatabase(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build())
                .withNoMessaging()
                .macheUp();
    }

    @Override
    public MongoAnnotatedMessage createEntity(String primaryKey, String msg) {
        return new MongoAnnotatedMessage(primaryKey, msg);
    }
}

