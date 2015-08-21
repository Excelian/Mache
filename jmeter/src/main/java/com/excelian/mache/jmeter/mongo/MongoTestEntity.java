package com.excelian.mache.jmeter.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class MongoTestEntity {
    @Id
    public String pkString = "key1";

    @Field(value = "description")
    public String description = "description for item";

    public MongoTestEntity(final String pkString, final String description) {
        this.pkString = pkString;
        this.description = description;
    }

    public MongoTestEntity() {
    }

    @Override
    public String toString() {
        return "[TestEntity key=" + pkString + " (hC=" + pkString.hashCode() + "), description=" + description + "]";
    }
}
