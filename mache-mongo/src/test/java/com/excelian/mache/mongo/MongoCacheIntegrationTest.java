package com.excelian.mache.mongo;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.excelian.mache.builder.MacheBuilder;
import com.excelian.mache.core.Mache;
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

import java.util.Date;

import static com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@IgnoreIf(condition = NoRunningMongoDbForTests.class)
public class MongoCacheIntegrationTest {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();


    private String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();
    private Mache<String, TestEntity> mache;

    @Before
    public void setUp() throws Exception {
        mache = getMache();
    }

    private Mache<String, TestEntity> getMache() throws Exception {
        return MacheBuilder.mache(String.class, TestEntity.class)
                .cachedBy(guava())
                .storedIn(mongodb()
                        .withSeeds(new ServerAddress(new NoRunningMongoDbForTests().getHost(), 27017))
                        .withDatabase(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build())
                .withNoMessaging()
                .macheUp();
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

    @Test(expected = CacheLoader.InvalidCacheLoadException.class)
    public void testRemove() throws Exception {
        String key = "rem-test-2";
        mache.put(key, new TestEntity(key));
        mache.remove(key);
        mache.get(key);
    }

    @Test
    public void testInvalidate() throws Exception {
        final Mache<String, TestEntity> mache = getMache();

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
        this.mache.put("test-2", new TestEntity("test-2"));
        this.mache.put("test-3", new TestEntity("test-3"));
        Mache<String, TestEntity> mache = getMache();

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
