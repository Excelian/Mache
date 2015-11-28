package com.excelian.mache.file;

import com.excelian.mache.core.Mache;
import com.excelian.mache.file.builder.FileProvisioner;
import com.google.common.cache.CacheLoader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.file.builder.FileProvisioner.file;

import static org.junit.Assert.*;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

public class FileCacheLoaderIntegrationTest {
    private static final double DELTA = 0.000001;

    private Mache<String, TestEntity> cache;
    private String cacheFilePath;

    @Before
    public void setup() throws Exception {
        cacheFilePath = "/tmp/mache-cache-file.txt";
        cache = mache(String.class, TestEntity.class)
            .backedBy(file()
                .storedAt(cacheFilePath))
            .withNoMessaging()
            .macheUp();
    }

    @After
    public void tearDown() {
        cache.close();
        final File cacheFile = new File(this.cacheFilePath);
        if (cacheFile.exists()) {
            assertTrue(cacheFile.delete());
        }
    }

    @Test
    public void ensureFileIsWrittenTo() {
        cache.put("test1", new TestEntity("test1", "FXRATE", 0.91));
        cache.put("test1", new TestEntity("test2", "FXRATE", 0.905));
        cache.put("test1", new TestEntity("test3", "FXRATE", 0.92));
        final File theCacheFile = new File(cacheFilePath);
        assertTrue("Cache file empty", theCacheFile.length() > 0);
    }

    @Test
    public void canPutAndGetValue() throws Throwable {
        cache.put("test1", new TestEntity("test1", "FXRATE", 0.91));
        assertEquals(0.91, cache.get("test1").value, DELTA);
    }

    @Test(expected = CacheLoader.InvalidCacheLoadException.class)
    public void canRemove() throws Throwable {
        TestEntity test2 = new TestEntity("test2", "FXRATE", 0.92);
        cache.put("test2", test2);
        assertEquals(test2, cache.get("test2"));
        cache.remove("test2");
        cache.get("test2");
    }

    @Test
    public void canOverwriteValue() throws Throwable {
        cache.put("test3", new TestEntity("test3", "FXRATE", 3.93));
        cache.put("test3", new TestEntity("test3", "FXRATE", 0.93));
        assertEquals(0.93, cache.get("test3").value, DELTA);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void ensureGetDriverSessionAlwaysReturnsValue() {
        FileCacheLoader<String, Object> loader = (FileCacheLoader) cache.getCacheLoader();
        loader.create();
        final String driverSession = loader.getDriverSession();
        assertNotNull(driverSession);
        loader.close();
        assertNotNull(loader.getDriverSession());
    }

    public static class TestEntity implements Serializable {
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
}

