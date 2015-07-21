package org.mache.jmeter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class TestEntity {
    @Id
    String pkString = "key1";

    @Field(value = "differentName")
    private String aString = "description for item";

    public TestEntity(String pkString) {
        this.pkString = pkString;
    }
}
