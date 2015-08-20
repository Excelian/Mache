package com.excelian.mache.jmeter.cassandra;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

@Table
public class CassandraTestEntity {

    @Column(value = "mappedColumn")
    public String description = "description for item";

    @PrimaryKey
    String pkString = "@Key1@";

    public CassandraTestEntity(String pkString, String description) {
        this.pkString = pkString;
        this.description = description;
    }
}
