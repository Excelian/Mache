package com.excelian.mache.couchbase;

import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.io.Serializable;
import java.util.Objects;

@Document
public class TestEntity implements Serializable {
    @Id
    String key;

    String type;

    double value;

    public TestEntity(String key, String type, double value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        TestEntity that = (TestEntity) other;
        return Objects.equals(value, that.value)
                && Objects.equals(key, that.key)
                && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type, value);
    }
}