package com.excelian.mache.core;

public class TestEntity2 {
    public final String pkey;
    public final String otherValue;

    public TestEntity2(String key, String otherValue) {
        this.pkey = key;
        this.otherValue = otherValue;
    }

    @Override
    public String toString() {
        return "TestEntity2 [pkey=" + pkey + ", otherValue=" + otherValue + "]";
    }
}
