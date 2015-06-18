package org.mache;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.*;

import java.io.Serializable;
import java.util.Date;

import static org.junit.Assert.*;

public class CassandraCacheIntegrationTest {

    private Cluster cluster = CassandraCacheLoader.connect("10.28.1.140", "BluePrint", 9042);
    private String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();
    private CacheThing<String, TestEntity> cacheThing;

    @Before
    public void setUp() throws Exception {
        cacheThing = new CacheThing<>(
                new CassandraCacheLoader<String,TestEntity>(TestEntity.class, cluster, true, keySpace));
    }

    @After
    public void tearDown() throws Exception {
        cacheThing.close();
    }

    @Test
    public void testGetDriver() throws Exception {
        cacheThing.get("test-1");
        CassandraCacheLoader cacheLoader = (CassandraCacheLoader) cacheThing.getCacheLoader();
        Session driver = cacheLoader.getDriverSession();
        assertNotNull(driver.getCluster());
    }

    @Test
    public void testPut() throws Exception {

        cacheThing.put("test-1", new TestEntity("value-yay"));
        TestEntity test = cacheThing.get("test-1");
        assertEquals("value-yay", test.pkString);
    }

    @Test
    public void testReadCache() throws Exception {

        cacheThing.put("test-2", new TestEntity("test-2"));
        TestEntity test = cacheThing.get("test-2");
        assertEquals("test-2", test.pkString);
    }

    @Test
    public void testRemove() throws Exception {
        String key = "rem-test-2";
        cacheThing.put(key, new TestEntity(key));
        cacheThing.remove(key);
        TestEntity testEntity = cacheThing.get(key);
        assertNull("Item wasnt removed", testEntity);
    }


    @Test
    public void testReadThrough() throws Exception {
        cacheThing.put("test-2", new TestEntity("test-2"));
        cacheThing.put("test-3", new TestEntity("test-3"));
        // replace the cache
        cacheThing = new CacheThing<String, TestEntity>(new CassandraCacheLoader<>(TestEntity.class, cluster, true, keySpace));

        TestEntity test = cacheThing.get("test-2");
        assertEquals("test-2", test.pkString);
    }

    @Table
    public static class TestEntity {
        @Column
        private int firstInt = 1;
        @Column
        private double aDouble = 1.0;
        @Column(value="mappedColumn")
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
        @Column(value="mappedColumn")
        private String aString = "yay";
        @PrimaryKey
        private CompositeKey compositeKey = new CompositeKey("a","b", "c");

        public TestEntityWithCompositeKey(){
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

        CacheThing<CompositeKey, TestEntityWithCompositeKey> compCache = new CacheThing<CompositeKey, TestEntityWithCompositeKey>(
                new CassandraCacheLoader<String,TestEntityWithCompositeKey>(TestEntityWithCompositeKey.class, cluster, true, keySpace));

        TestEntityWithCompositeKey value = new TestEntityWithCompositeKey("neil", "mac", "explorer");
        compCache.put(value.compositeKey, value);

        TestEntityWithCompositeKey testValue = compCache.get(value.compositeKey);
        assertEquals("neil", testValue.compositeKey.personId);

        compCache.close();
    }
}