package com.excelian.mache.examples.cassandra;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

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
        final StringBuilder sb = new StringBuilder("Message{");
        sb.append("msg='").append(msg).append('\'');
        sb.append(", primaryKey='").append(primaryKey).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
