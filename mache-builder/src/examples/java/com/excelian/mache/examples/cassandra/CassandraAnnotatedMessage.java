package com.excelian.mache.examples.cassandra;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

/**
 * Example Spring Data Cassandra Annotated Object.
 */
@Table
public class CassandraAnnotatedMessage {

    private final String msg;

    @PrimaryKey
    private final String primaryKey;

    public CassandraAnnotatedMessage(String primaryKey, String msg) {
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
