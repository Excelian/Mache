package com.excelian.mache.cassandra;

import com.excelian.mache.core.MacheImpl;
import com.excelian.mache.core.MacheLoader;
import com.google.common.cache.CacheLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.*;

import java.io.Serializable;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/*
Core common set of tests that all CacheLoader classes should pass
 */
public abstract class TestCacheLoaderBase {

    protected String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();
    private MacheImpl<String, TestEntity> mache;

    abstract protected MacheLoader buildCacheLoader(Class cls) throws Exception;

    @Before
    public void setUp() throws Exception {
        mache = new MacheImpl<String, TestEntity>(buildCacheLoader(TestEntity.class));
    }

    @After
    public void tearDown() throws Exception {
        mache.close();
    }

    @Test
    public void testCanGetDriverSession() throws Exception {
        MacheLoader cacheloader = buildCacheLoader(TestEntity.class);
        MacheImpl cache = new MacheImpl<String, TestEntity>(cacheloader);
        cache.put("test-2", new TestEntity("test-2"));
        cache.get("test-2");
        assertNotNull(cacheloader.getDriverSession());

        cacheloader.close();
        cache.close();
    }

    @Test
    public void testPut() throws Exception {
        mache.put("value-yay", new TestEntity("value-yay"));
        TestEntity test = mache.get("value-yay");
        assertNotNull("Expected object to be retrieved from cache", test);
        assertEquals("value-yay", test.pkString);
    }

    @Test
    public void canPutTheSameItemAgainTest() throws Exception {
        mache.put("test-1", new TestEntity("test-1"));
        mache.put("test-1", new TestEntity("test-1"));
        TestEntity test = mache.get("test-1");
        assertEquals("test-1", test.pkString);
    }

    @Test
    public void testReadCache() throws Exception {
        mache.put("test-2", new TestEntity("test-2"));
        TestEntity test = mache.get("test-2");
        assertEquals("test-2", test.pkString);
    }

    @Test
    public void testRemove() throws Exception {
        CacheLoader.InvalidCacheLoadException exception = null;
        String key = "rem-test-2";
        mache.put(key, new TestEntity(key));
        assertNotNull("Expected entry to be in cache prior to remocal", mache.get(key));

        mache.remove(key);

        try {
            mache.get(key);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            exception = e;
        }

        assertNotNull("Exception expected to have been thrown", exception);
        assertEquals("CacheLoader returned null for key rem-test-2.", exception.getMessage());
    }

    @Test
    public void testReadThrough() throws Exception {
        mache.put("test-2", new TestEntity("test-2"));
        mache.put("test-3", new TestEntity("test-3"));
        // replace the cache
        mache = new MacheImpl<String, TestEntity>(buildCacheLoader(TestEntity.class));

        TestEntity test = mache.get("test-2");
        assertEquals("test-2", test.pkString);
    }

    @Table
    public static class TestEntity {
        @Column
        private int firstInt = 1;
        @Column
        private double aDouble = 1.0;
        @Column(value = "mappedColumn")
        private String aString = "yay";
        @PrimaryKey
        String pkString = "yay";

        public TestEntity(String pkString) {
            this.pkString = pkString;
        }
    }

    @Table
    public static class TestEntityWithCompositeKey {
        @Column
        private int firstInt = 1;
        @Column
        private double aDouble = 1.0;
        @Column(value = "mappedColumn")
        private String aString = "yay";
        @PrimaryKey
        private CompositeKey compositeKey = new CompositeKey("a", "b", "c");

        public TestEntityWithCompositeKey() {
        }

        public TestEntityWithCompositeKey(String person, String workstation, String app) {
            compositeKey = new CompositeKey(person, workstation, app);
        }
    }

    @PrimaryKeyClass
    public static class CompositeKey implements Serializable {

        public CompositeKey() {
        }

        public CompositeKey(String personId, String workstationId, String application) {

            this.personId = personId;
            this.workstationId = workstationId;
            this.application = application;
        }

        @PrimaryKeyColumn(name = "person_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private String personId;

        @PrimaryKeyColumn(name = "wks_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        private String workstationId;

        @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
        private String application;
    }

    @Test
    public void testPutComposite() throws Exception {

        MacheImpl<CompositeKey, TestEntityWithCompositeKey> compCache = new MacheImpl<CompositeKey, TestEntityWithCompositeKey>(
                buildCacheLoader(TestEntityWithCompositeKey.class));

        TestEntityWithCompositeKey value = new TestEntityWithCompositeKey("neil", "mac", "explorer");
        compCache.put(value.compositeKey, value);

        TestEntityWithCompositeKey testValue = compCache.get(value.compositeKey);
        assertEquals("neil", testValue.compositeKey.personId);

        compCache.close();
    }
}