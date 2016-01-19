package com.excelian.mache.examples.mongo;

import com.excelian.mache.examples.Example;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Example Spring Data Mongo Annotated Object.
 */
@Document
public class MongoAnnotatedMessage implements Example.KeyedMessge{

    @Field
    private final String msg;

    @Id
    private final String primaryKey;

    public MongoAnnotatedMessage(String primaryKey, String msg) {
        this.msg = msg;
        this.primaryKey = primaryKey;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public String toString() {
        return "Message{" + "msg='" + msg + '\'' + ", primaryKey='" + primaryKey + '\'' + '}';
    }
}
