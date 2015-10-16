package com.excelian.mache.jmeter.couch;

import com.couchbase.client.java.repository.annotation.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

/**
 * Example Spring Data Couchbase Annotated Object.
 */
@Document
public class CouchTestEntity {

    @Id
    public String pkString = "key1";

    @Field(value = "description")
    public String description = "description for item";

    public CouchTestEntity(final String pkString, final String description) {
        this.pkString = pkString;
        this.description = description;
    }

    public CouchTestEntity() {
    }

    @Override
    public String toString() {
        return "[TestEntity key=" + pkString + " (hC=" + pkString.hashCode() + "), description=" + description + "]";
    }
}
