package org.mache.jmeter;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class MongoTestEntity {
    @Id
    String pkString = "key1";

    @Field(value = "differentName")
    public String description = "description for item";

    public MongoTestEntity(String pkString) {
        this.pkString = pkString;
    }
}

