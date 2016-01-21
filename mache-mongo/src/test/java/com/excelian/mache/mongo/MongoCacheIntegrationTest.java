package com.excelian.mache.mongo;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.excelian.mache.builder.MacheBuilder;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.google.common.cache.CacheLoader;
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
import java.util.List;

import static com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongoConnectionContext;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@IgnoreIf(condition = NoRunningMongoDbForTests.class)
public class MongoCacheIntegrationTest {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();


    private String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();

    private Mache<String, TestEntity> mache;
    private ConnectionContext<List<ServerAddress>> connectionContext;

    @Before
    public void setUp() throws Exception {
        connectionContext = mongoConnectionContext(new ServerAddress(new NoRunningMongoDbForTests().getHost(), 27017));
        mache = getMache(connectionContext);
    }

    private Mache<String, TestEntity> getMache(ConnectionContext<List<ServerAddress>> context) throws Exception {

        return MacheBuilder.mache(String.class, TestEntity.class)
                .cachedBy(guava())
                .storedIn(mongodb()
                        .withConnectionContext(context)
                        .withDatabase(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build())
                .withNoMessaging()
                .macheUp();
    }

    @After
    public void tearDown() throws Exception {
        mache.close();
        connectionContext.close();
    }

    @Test
    @IgnoreIf(condition = NoRunningMongoDbForTests.class)
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

    @Test
    public void testRemove() throws Exception {
        String key = "rem-test-2";
        mache.put(key, new TestEntity(key));
        mache.remove(key);
        assertEquals(null, mache.get(key));
    }

    @Test
    public void testInvalidate() throws Exception {
        try(final Mache<String, TestEntity> anotherMache = getMache(connectionContext)) {

            final String key = "test-1";
            final String expectedDescription = "test1-description";
            this.mache.put(key, new TestEntity(key, expectedDescription));
            assertEquals(expectedDescription, this.mache.get(key).getaString());
            assertEquals(expectedDescription, anotherMache.get(key).getaString());

            final String expectedDescription2 = "test-description2";
            anotherMache.put(key, new TestEntity(key, expectedDescription2));
            this.mache.invalidate(key);
            assertEquals(expectedDescription2, this.mache.get(key).getaString());
            assertEquals(expectedDescription2, anotherMache.get(key).getaString());
        }
    }

    @Test
    public void testReadThrough() throws Exception {
        this.mache.put("test-2", new TestEntity("test-2"));
        this.mache.put("test-3", new TestEntity("test-3"));

        try(Mache<String, TestEntity> anotherMacheInstance = getMache(connectionContext)) {

            TestEntity test = anotherMacheInstance.get("test-2");
            assertEquals("test-2", test.pkString);
        }
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
