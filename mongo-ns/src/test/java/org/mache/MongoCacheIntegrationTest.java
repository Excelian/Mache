package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.google.common.cache.CacheLoader;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import org.junit.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.codeaffine.test.ConditionalIgnoreRule.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by neil.avery on 09/06/2015.
 */
@IgnoreIf(condition = NotRunningInExcelian.class)
public class MongoCacheIntegrationTest {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private List<ServerAddress> serverAddresses = Arrays.asList(new ServerAddress("10.28.1.140", 27017));
    private String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();
    private CacheThing<String, TestEntity> cacheThing;

    @Before
    public void setUp() throws Exception {
        cacheThing = new CacheThing<String, TestEntity>(
                new MongoDBCacheLoader<String,TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));
    }

    @After
    public void tearDown() throws Exception {
        cacheThing.close();
    }

    @Test
    public void testGetDriver() throws Exception {
        cacheThing.put("test-1", new TestEntity("test-1"));
        cacheThing.get("test-1");
        MongoDBCacheLoader cacheLoader = (MongoDBCacheLoader) cacheThing.getCacheLoader();
        Mongo driver = cacheLoader.getDriverSession();
        assertNotNull(driver.getAddress());
    }

    @Test
    public void testPut() throws Exception {
        cacheThing.put("test-1", new TestEntity("test-1"));
        TestEntity test = cacheThing.get("test-1");
        assertEquals("test-1", test.pkString);
    }

    @Test
    public void canPutTheSameItemAgainTest() throws Exception {
        cacheThing.put("test-1", new TestEntity("test-1"));
        cacheThing.put("test-1", new TestEntity("test-1"));//TODO: This should be passing
        TestEntity test = cacheThing.get("test-1");
        assertEquals("test-1", test.pkString);
    }

    @Test
    public void testReadCache() throws Exception {
        cacheThing.put("test-2", new TestEntity("test-2"));
        TestEntity test = cacheThing.get("test-2");
        assertEquals("test-2", test.pkString);
    }

    @Test
    public void testRemove() throws Exception {
        CacheLoader.InvalidCacheLoadException exception=null;
        String key = "rem-test-2";
        cacheThing.put(key, new TestEntity(key));
        cacheThing.remove(key);

        try {
            cacheThing.get(key);
        }
        catch(CacheLoader.InvalidCacheLoadException e) {
            exception=e;
        }

        assertNotNull("Exception expected to have been thrown", exception);
        assertEquals("CacheLoader returned null for key rem-test-2.", exception.getMessage());
    }

    @Test
    public void testInvalidate() throws Exception {
        final CacheThing<String, TestEntity> cacheThing2 = new CacheThing<>(
                new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));

        final String key = "test-1";
        final String expectedDescription = "test1-description";
        cacheThing.put(key, new TestEntity(key, expectedDescription));
        assertEquals(expectedDescription, cacheThing.get(key).getaString());
        assertEquals(expectedDescription, cacheThing2.get(key).getaString());

        final String expectedDescription2 = "test-description2";
        cacheThing2.put(key, new TestEntity(key, expectedDescription2));
        cacheThing.invalidate(key);
        assertEquals(expectedDescription2, cacheThing.get(key).getaString());
        assertEquals(expectedDescription2, cacheThing2.get(key).getaString());

        cacheThing2.close();
    }

    @Test
    public void testReadThrough() throws Exception {
        cacheThing.put("test-2", new TestEntity("test-2"));
        cacheThing.put("test-3", new TestEntity("test-3"));
        // replace the cache
        Thread.sleep(1000);
        CacheThing<String, TestEntity> cacheThing1 = new CacheThing<String, TestEntity>(
                new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));

        TestEntity test = cacheThing1.get("test-2");
        assertEquals("test-2", test.pkString);

        cacheThing1.close();
    }

    /**
     *  @see "http://docs.spring.io/spring-data/data-mongo/docs/1.8.0.M1/reference/html/#mapping-usage"
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

        public TestEntity()
        {
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
