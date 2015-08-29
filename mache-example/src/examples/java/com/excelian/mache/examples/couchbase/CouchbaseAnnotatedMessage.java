package com.excelian.mache.examples.couchbase;

import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

/**
 * Example Spring Data Couchbase Annotated Object.
 */
@Document
public class CouchbaseAnnotatedMessage {

    private final String msg;

    @Id
    private final String primaryKey;

    public CouchbaseAnnotatedMessage(String primaryKey, String msg) {
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
