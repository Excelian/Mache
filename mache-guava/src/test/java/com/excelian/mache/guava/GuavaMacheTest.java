package com.excelian.mache.guava;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GuavaMacheTest {

    private int read;
    private int removed;
    private int put;
    private Mache<String, String> mache;

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

        MacheLoader<String, String, String> fixture = new MacheLoader<String, String, String>() {
            public String load(String key) throws Exception {
                read++;
                return "FIXTURE:loaded_" + key;
            }

            public void close() {
            }

            @Override
            public void create() {
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

        mache = GuavaMacheProvisioner.<String, String>guava().create(String.class, String.class, fixture);
    }
}
