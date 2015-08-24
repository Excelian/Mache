package com.excelian.mache.core;

import com.google.common.cache.RemovalNotification;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MacheImplTest {

    private AbstractCacheLoader<String, String, String> fixture;
    private int created;
    private int read;
    private int removed;
    private RemovalNotification receivedNotification;
    private int put;
    private MacheImpl<String, String> mache;

    @Test
    public void canCreate() throws ExecutionException {

        String test = mache.get("TEST1");
        test = mache.get("TEST2");
        assertEquals(1, created);
    }


    @Test
    public void canReadThrough() throws ExecutionException {
        String test = mache.get("TEST");
        assertEquals(1, read);
        assertEquals("FIXTURE:loaded_TEST", test);
    }

    @Test
    public void readsThroughOnceThenOnlyReadsFromCacheForSameKey() throws Exception {

        assertNotNull(mache.get("TEST"));
        assertNotNull(mache.get("TEST"));
        assertNotNull(mache.get("TEST"));

        assertEquals("Expected loader to have only been called once (the first time)", 1, read);
    }

    @Test
    public void canWriteThrough() throws ExecutionException {
        mache.put("TEST", "VALUE");
        assertEquals(1, put);
    }

    @Test
    public void canRemove() throws ExecutionException {
        mache.put("TEST", "VALUE");
        mache.remove("TEST");
        assertEquals(1, removed);
    }

    @Test
    public void invalidateWorks() {
        final String key = "TEST";
        mache.put(key, "VALUE");
        mache.invalidate(key);
        mache.get(key);

        assertEquals(1, read);
    }

    @Test
    public void multipleInvalidateWorks() {
        final String key = "TEST";
        mache.put(key, "VALUE");

        final int invalidateTimes = 3;

        for (int i = 0; i < invalidateTimes; ++i) {
            mache.invalidate(key);
            mache.get(key);
        }

        assertEquals(invalidateTimes, read);
    }

    @Before
    public void setUp() throws Exception {

        fixture = new AbstractCacheLoader<String, String, String>() {
            public String load(String key) throws Exception {
                read++;
                return "FIXTURE:loaded_" + key;
            }

            public void close() {
            }

            @Override
            public void create() {
                created++;
            }

            public void put(String s, String s2) {
                put++;
            }

            public void remove(String s) {
                removed++;
            }

            public String getName() {
                return "myCache";
            }

            public String getDriverSession() {
                return "yay";
            }

            public void invalidateAll() {
            }
        };
        mache = new MacheImpl<String, String>(fixture);
    }
}
