package com.excelian.mache.examples.mongo;

import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;
import com.mongodb.ServerAddress;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongoConnectionContext;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;

/**
 * A factory for a Mongo backed {@link Example}.
 */
public class MongoExample implements Example<MongoAnnotatedMessage, List<ServerAddress> , MongoAnnotatedMessage > {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private String serverIpAddress;

    public MongoExample(String serverIpAddress)
    {
        this.serverIpAddress = serverIpAddress;
    }

    @Override
    public Mache<String, MongoAnnotatedMessage> exampleCache(ConnectionContext connectionContext) throws Exception {
        final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
        return mache(String.class, MongoAnnotatedMessage.class)
                .backedBy(mongodb()
                        .withContext(connectionContext)
                        .withDatabase(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build())
                .withNoMessaging()
                .macheUp();
    }

    @Override
    public ConnectionContext<List<ServerAddress>> createConnectionContext() {
        return mongoConnectionContext(new ServerAddress(serverIpAddress, 9042));
    }

    @Override
    public MongoAnnotatedMessage createEntity(String primaryKey, String msg) {
        return new MongoAnnotatedMessage(primaryKey, msg);
    }
}

