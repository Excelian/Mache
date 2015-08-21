package com.excelian.mache.mongo;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.excelian.mache.core.MacheImpl;
import com.excelian.mache.core.NoRunningMongoDbForTests;
import com.excelian.mache.core.SchemaOptions;
import com.google.common.cache.CacheLoader;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@IgnoreIf(condition = NoRunningMongoDbForTests.class)
public class MongoCacheIntegrationTest {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();


    private String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();
    private MacheImpl<String, TestEntity> mache;

    @Before
    public void setUp() throws Exception {
        List<ServerAddress> serverAddresses = Arrays.asList(new ServerAddress(new NoRunningMongoDbForTests().HostName(), 27017));
        mache = new MacheImpl<String, TestEntity>(
                new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));
    }

    @After
    public void tearDown() throws Exception {
        mache.close();
    }

    @Test
    public void testGetDriver() throws Exception {
        mache.put("test-1", new TestEntity("test-1"));
        mache.get("test-1");
        MongoDBCacheLoader cacheLoader = (MongoDBCacheLoader) mache.getCacheLoader();
        Mongo driver = cacheLoader.getDriverSession();
        assertNotNull(driver.getAddress());
    }

    @Test
    public void testPut() throws Exception {
        mache.put("test-1", new TestEntity("test-1"));
        TestEntity test = mache.get("test-1");
        assertEquals("test-1", test.pkString);
    }

    @Test
    public void canPutTheSameItemAgainTest() throws Exception {
        mache.put("test-1", new TestEntity("test-1"));
        mache.put("test-1", new TestEntity("test-1"));//TODO: This should be passing
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
    public void testInvalidate() throws Exception {
        List<ServerAddress> serverAddresses = Arrays.asList(new ServerAddress(new NoRunningMongoDbForTests().HostName(), 27017));
        final MacheImpl<String, TestEntity> mache = new MacheImpl<>(
                new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));

        final String key = "test-1";
        final String expectedDescription = "test1-description";
        this.mache.put(key, new TestEntity(key, expectedDescription));
        assertEquals(expectedDescription, this.mache.get(key).getaString());
        assertEquals(expectedDescription, mache.get(key).getaString());

        final String expectedDescription2 = "test-description2";
        mache.put(key, new TestEntity(key, expectedDescription2));
        this.mache.invalidate(key);
        assertEquals(expectedDescription2, this.mache.get(key).getaString());
        assertEquals(expectedDescription2, mache.get(key).getaString());

        mache.close();
    }

    @Test
    public void testReadThrough() throws Exception {
        List<ServerAddress> serverAddresses = Arrays.asList(new ServerAddress(new NoRunningMongoDbForTests().HostName(), 27017));
        this.mache.put("test-2", new TestEntity("test-2"));
        this.mache.put("test-3", new TestEntity("test-3"));
        // replace the cache
        Thread.sleep(1000);
        MacheImpl<String, TestEntity> mache = new MacheImpl<String, TestEntity>(
                new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));

        TestEntity test = mache.get("test-2");
        assertEquals("test-2", test.pkString);

        mache.close();
    }

    /**
     * @see "http://docs.spring.io/spring-data/data-mongo/docs/1.8.0.M1/reference/html/#mapping-usage"
     */
    @Document
    public static class TestEntity {
        @Id
        String pkString = "yay";

        private int firstInt = 1;

        @Field(value = "differentName")
        private double aDouble = 1.0;

        @Indexed
        private String aString = "yay";

        public TestEntity() {
            /*Default constructor required by mongo driver (findbyid call) */
        }

        public TestEntity(String pkString) {
            this.pkString = pkString;
        }

        public TestEntity(String pkString, String other) {
            this.pkString = pkString;
            this.aString = other;
        }

        public String getaString() {
            return aString;
        }
    }
}
