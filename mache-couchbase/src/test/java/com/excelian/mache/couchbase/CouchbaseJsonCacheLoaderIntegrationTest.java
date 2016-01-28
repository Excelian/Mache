package com.excelian.mache.couchbase;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.core.Mache;
import com.excelian.mache.couchbase.builder.CouchbaseConnectionContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.couchbase.client.java.cluster.DefaultBucketSettings.builder;
import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.caffeine.CaffeineMacheProvisioner.caffeine;
import static com.excelian.mache.core.SchemaOptions.CREATE_SCHEMA_IF_NEEDED;
import static com.excelian.mache.couchbase.builder.CouchbaseConnectionContext.*;
import static com.excelian.mache.couchbase.builder.CouchbaseProvisioner.couchbase;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ConditionalIgnoreRule.IgnoreIf(condition = NoRunningCouchbaseDbForTests.class)
public class CouchbaseJsonCacheLoaderIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(CouchbaseJsonCacheLoaderIntegrationTest.class);

    private static final String BUCKET = "couchbase-test";
    private static final String ADMIN_USER = "Administrator";
    private static final String PASSWORD = "password";
    private static final double DELTA = 0.000001;
    private static final String COUCHBASE_HOST = new NoRunningCouchbaseDbForTests().getHost();

    private static final CouchbaseEnvironment COUCHBASE_ENVIRONMENT =
        DefaultCouchbaseEnvironment.builder().connectTimeout(SECONDS.toMillis(100)).build();
    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    private Mache<String, String> couchbaseJsonMache;
    private CouchbaseConnectionContext connectionContext;
    private String resultFromDatabase;
    private String cachedValueForKey;

    public Mache<String, String> exampleCache() throws Exception {
        return mache(String.class, String.class)
            .cachedBy(caffeine())
            .storedIn(couchbase()
                .withBucketSettings(builder().name(BUCKET).quota(150).build())
                .withCouchbaseEnvironment(COUCHBASE_ENVIRONMENT)
                .withAdminDetails(ADMIN_USER, PASSWORD)
                .withNodes(COUCHBASE_HOST)
                .withSchemaOptions(CREATE_SCHEMA_IF_NEEDED)
                .asJsonDocuments())
            .withNoMessaging()
            .macheUp();
    }

    @Before
    public void executeBeforeEachTest() throws Exception {
        couchbaseJsonMache = exampleCache();
        connectionContext = getInstance(COUCHBASE_ENVIRONMENT, singletonList(COUCHBASE_HOST));
    }

    @After
    public void executeAfterEachTest() {
        getBucket().bucketManager().flush();
        if (couchbaseJsonMache != null) {
            couchbaseJsonMache.close();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAJsonMacheCannotBeInstantiatedThatIsNotOfTypeStringString() throws Exception {
        mache(String.class, TestEntity.class)
            .cachedBy(caffeine())
            .storedIn(couchbase()
                .withBucketSettings(builder().name(BUCKET).quota(150).enableFlush(true).build())
                .withCouchbaseEnvironment(COUCHBASE_ENVIRONMENT)
                .withAdminDetails(ADMIN_USER, PASSWORD)
                .withNodes(COUCHBASE_HOST)
                .withSchemaOptions(CREATE_SCHEMA_IF_NEEDED)
                .asJsonDocuments())
            .withNoMessaging()
            .macheUp();
    }

    @Test
    public void ensureAJsonDocumentCanBeReadAsJsonFromExistingRecords()
        throws Exception {
        given_anInsertedRecordWithJson("user123-JSON", jsonDoc("user123-JSON", 44, "TX"));
        when_theCacheIsQueriedForKey("user123-JSON");
        then_theValueReadIs(jsonDoc("user123-JSON", 44, "TX"));
    }

    @Test
    public void ensureAJsonDocumentCanBeWrittenBackToTheTableFromTheCache() throws Exception {
        final String jsonDocValue = jsonDoc("new-key-123", 99, "MA");
        given_TheCachePut("new-key-123", jsonDocValue);
        when_theDatabaseIsQueriedForKey("new-key-123");
        then_theValueRetrievedFromTheDatabaseIs(jsonDocValue);
    }

    @Test
    public void ensureAJsonDocumentCanBeDeletedFromTheTableByTheCacheLoader() throws Exception {
        given_anInsertedRecordWithJson("user123-JSON", jsonDoc("user123-JSON", 44, "TX"));
        given_theCacheIsWarmedWithTheKey("user123-JSON");
        when_removeIsCalledOnTheCacheWithKey("user123-JSON");
        then_theDatabaseContainsNullForKey("user123-JSON");
    }

    @Test
    public void ensureAJsonDocumentCanBeUpdatedInTheTableByTheCacheLoader() throws Exception {
        given_anInsertedRecordWithJson("user123-JSON", jsonDoc("user123-JSON", 44, "TX"));
        given_theCacheIsWarmedWithTheKey("user123-JSON");
        when_putIsCalledOnTheCacheWithKeyAndValue("user123-JSON", jsonDoc("user123-JSON", 145, "MA"));
        then_theDatabaseContainsTheValueForForKey("user123-JSON", jsonDoc("user123-JSON", 145, "MA"));
    }

    @Test
    public void ensureANonExistentValueInTheDBAndTheCacheYieldsNullFromGet() throws Exception {
        when_theCacheIsQueriedForKey("NON-EXISTENT");
        then_theValueReadIs(null);
    }

    private Bucket getBucket() {
        final Cluster connection = getConnection();
        return connection.openBucket(BUCKET);
    }

    private Cluster getConnection() {
        return connectionContext.getConnection(couchbaseJsonMache.getCacheLoader());
    }

    private void then_theDatabaseContainsTheValueForForKey(String key, String expectedJsonDoc) {
        when_theDatabaseIsQueriedForKey(key);
        assertEquals(expectedJsonDoc, this.resultFromDatabase);
    }

    private void when_putIsCalledOnTheCacheWithKeyAndValue(String key, String jsonDoc) {
        couchbaseJsonMache.put(key, jsonDoc);
    }

    private String jsonDoc(final String id, final int age, final String state) {
        return "{\"age\":" + age + ",\"id\":\"" + id + "\",\"state\":\"" + state + "\"}";
    }

    private void then_theDatabaseContainsNullForKey(String key) {
        when_theDatabaseIsQueriedForKey(key);
        assertNull(resultFromDatabase);
    }

    private void when_removeIsCalledOnTheCacheWithKey(String key) {
        this.couchbaseJsonMache.remove(key);
    }

    private void given_theCacheIsWarmedWithTheKey(String key) {
        when_theCacheIsQueriedForKey(key);
    }

    private void then_theValueRetrievedFromTheDatabaseIs(String expectedValue) {
        assertEquals(expectedValue, this.resultFromDatabase);
    }

    private void when_theDatabaseIsQueriedForKey(String key) {
        final JsonDocument jsonDocument = getBucket().get(key);
        if (jsonDocument == null) {
            this.resultFromDatabase = null;
        } else {
            final JsonObject content = jsonDocument.content();
            this.resultFromDatabase = content.toString();
        }
    }

    private void given_TheCachePut(String key, String value) {
        couchbaseJsonMache.put(key, value);
    }

    private void then_theValueReadIs(String expectedValue) {
        assertEquals(expectedValue, cachedValueForKey);
    }

    private void when_theCacheIsQueriedForKey(String key) {
        cachedValueForKey = couchbaseJsonMache.get(key);
    }

    private void given_anInsertedRecordWithJson(String key, String jsonValue) {
        final JsonObject jsonObject = JsonObject.fromJson(jsonValue);
        final JsonDocument jsonDocument = JsonDocument.create(key, jsonObject);
        getBucket().upsert(jsonDocument);
    }
}
