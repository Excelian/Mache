package com.excelian.mache.jmeter.cassandra;

import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

/**
 * Provides a test entity for JMeter testing.
 */
@Table
public class CassandraTestEntity {
    @PrimaryKey
    public String pkString = "yay";
    
    @Column
    public int firstInt = 1;
    
    @Column
    public double aDouble = 1.0;
    
    @Column(value = "mappedColumn")
    public String aString = "yay";
    
    public CassandraTestEntity() {}

    public CassandraTestEntity(final String pkString, final String value) {
        this.pkString = pkString;
        this.aString = value;
    }
}
