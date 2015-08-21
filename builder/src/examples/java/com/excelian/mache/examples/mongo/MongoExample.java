package com.excelian.mache.examples.mongo;

import com.excelian.mache.builder.Builder;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jbowkett on 17/07/15.
 */
public class MongoExample implements Example<MongoAnnotatedMessage> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Override
    public Mache<String, MongoAnnotatedMessage> exampleCache() {
        final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
        return Builder.mache()
            .backedByMongo()
            .at(Builder.server("10.28.1.140", 9042))
            .withKeyspace(keySpace)
            .toStore(MongoAnnotatedMessage.class)
            .withPolicy(SchemaOptions.CREATEANDDROPSCHEMA)
            .withNoMessaging()
            .macheUp();
    }

}

