package org.mache.jmeter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class TestEntity {
    @Id
    String pkString = "key1";

    @Field(value = "description")
    private String description = "description for item";

    public TestEntity(final String pkString, final String description) {
        this.pkString = pkString;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "[TestEntity key=" + pkString + " (hC="+ pkString.hashCode() +"), description=" + description + "]";
    }
}
