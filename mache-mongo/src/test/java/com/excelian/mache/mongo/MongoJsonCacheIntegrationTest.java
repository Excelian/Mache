package com.excelian.mache.mongo;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.excelian.mache.builder.MacheBuilder;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;

import static com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@IgnoreIf(condition = NoRunningMongoDbForTests.class)
public class MongoJsonCacheIntegrationTest {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();


    private String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();
    private Mache<String, String> mache;

    @Before
    public void setUp() throws Exception {
        mache = getMache();
    }

    private Mache<String, String> getMache() throws Exception {
        return MacheBuilder.mache(String.class, String.class)
                .cachedBy(guava())
                .storedIn(mongodb()
                        .withSeeds(new ServerAddress(new NoRunningMongoDbForTests().getHost(), 27017))
                        .withDatabase(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                    .asJsonDocuments()
                    .inCollection("test")
                )
                .withNoMessaging()
                .macheUp();
    }

    @After
    public void tearDown() throws Exception {
        mache.close();
    }

    @Test
    public void testPut() throws Exception {
        mache.put("test-1", getJsonKey("test-1"));
        String test = mache.get("test-1");
        assertEquals("test-1", ((DBObject) JSON.parse(test)).get("_id"));
    }

    @Test
    public void canPutTheSameItemAgainTest() throws Exception {
        mache.put("test-1", getJsonKey("test-1"));
        mache.put("test-1", getJsonKey("test-1"));
        String test = mache.get("test-1");
        assertEquals("test-1", ((DBObject) JSON.parse(test)).get("_id"));
    }

    @Test
    public void testReadCache() throws Exception {
        mache.put("test-2", getJsonKey("test-2"));
        String test = mache.get("test-2");
        assertEquals("test-2", ((DBObject) JSON.parse(test)).get("_id"));
    }

    @Test
    public void testRemove() throws Exception {
        String key = "rem-test-2";
        mache.put(key, getJsonKey(key));
        mache.remove(key);
        assertNull(mache.get(key));
    }

    @Test
    public void testInvalidate() throws Exception {
        final Mache<String, String> mache = getMache();

        final String key = "test-1";
        final String expectedDescription = "test1-description";
        this.mache.put(key, getJsonKey(key, expectedDescription));
        assertEquals(expectedDescription, ((DBObject) JSON.parse(this.mache.get(key))).get("value"));
        assertEquals(expectedDescription, ((DBObject) JSON.parse(mache.get(key))).get("value"));


        final String expectedDescription2 = "test-description2";
        mache.put(key, getJsonKey(key, expectedDescription2));
        this.mache.invalidate(key);
        assertEquals(expectedDescription2, ((DBObject) JSON.parse(this.mache.get(key))).get("value"));
        assertEquals(expectedDescription2, ((DBObject) JSON.parse(mache.get(key))).get("value"));

        mache.close();
    }

    @Test
    public void testReadThrough() throws Exception {
        this.mache.put("test-2", getJsonKey("test-2"));
        this.mache.put("test-3", getJsonKey("test-3"));
        Mache<String, String> mache = getMache();

        String test = mache.get("test-2");
        assertEquals("test-2", ((DBObject) JSON.parse(test)).get("_id"));

        mache.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAJsonMacheCannotBeInstantiatedThatIsNotOfTypeStringString() throws Exception {
        MacheBuilder.mache(String.class, Integer.class)
            .cachedBy(guava())
            .storedIn(mongodb()
                .withSeeds(new ServerAddress(new NoRunningMongoDbForTests().getHost(), 27017))
                .withDatabase(keySpace)
                .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                .asJsonDocuments()
                .inCollection("test")
            )
            .withNoMessaging()
            .macheUp();
    }



    private String getJsonKey(String key) {
        return "{'_id': '" + key + "'}";
    }

    private String getJsonKey(String key, String value) {
        return "{'_id': '" + key + "', 'value': '" + value + "'}";
    }
}
