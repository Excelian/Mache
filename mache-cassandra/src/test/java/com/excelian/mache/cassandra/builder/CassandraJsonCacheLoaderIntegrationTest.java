package com.excelian.mache.cassandra.builder;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.excelian.mache.cassandra.NoRunningCassandraDbForTests;
import com.excelian.mache.core.Mache;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraConnectionContext.getInstance;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.core.SchemaOptions.CREATE_SCHEMA_IF_NEEDED;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ConditionalIgnoreRule.IgnoreIf(condition = NoRunningCassandraDbForTests.class)
public class CassandraJsonCacheLoaderIntegrationTest {

    private static final NoRunningCassandraDbForTests CASSANDRA_BLUEPRINT = new NoRunningCassandraDbForTests();

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private CassandraConnectionContext connectionContext;
    private Mache<String, String> cache;
    private String cachedValueForKey;
    private String resultFromDatabase;
    public static final String KEY_SPACE = "mache_json";

    /*
     * CQL Commands:
     * CREATE KEYSPACE "mache_json"
     *    WITH replication = {'class' : 'SimpleStrategy', 'replication_factor': 1};
     *
     * CREATE TABLE users (
     *    id text PRIMARY KEY,
     *    age int,
     *    state text
     *  );
     *
     *  INSERT INTO users (id, age, state) VALUES ('user123', 42, 'TX');
     *  INSERT INTO users JSON '{"id": "userJ123", "age": 42, "state": "TX"}';
     *  SELECT * FROM users;
     *  DELETE FROM users WHERE id = 'user123';
     *  TRUNCATE users;
     *
     *  SELECT JSON * FROM users;
     *  SELECT * FROM users;
     *  SELECT JSON * FROM users WHERE id = 'userJ123';
     */


    @Before
    public void executeBeforeEachTest() throws Exception {
        cache = exampleCache();
        connectionContext = getInstance(theCluster());
        getSession().execute(createTable());
    }

    private String createTable() {
        return format("CREATE TABLE if not exists %s.users "
            + "(id text PRIMARY KEY, age int, state text);", KEY_SPACE);
    }

    @After
    public void executeAfterEachTest() {
        getSession().execute(dropTable());
        if (cache != null) {
            cache.close();
        }
    }

    private String dropTable() {
        return format("DROP TABLE if exists %s.users;", KEY_SPACE);
    }

    @Test
    public void ensureAJsonDocumentCanBeReadAsJsonFromExistingRecordsWhenThatRecordWasNotInsertedAsJson()
        throws Exception {
        given_anInsertedRecordWithRawColumnValues();
        when_theCacheIsQueriedForKey("user123");
        then_theValueReadIs(jsonDoc("user123", 42, "TX"));
    }

    @Test
    public void ensureAJsonDocumentCanBeReadAsJsonFromExistingRecordsWhenThatRecordWasInsertedAsJson()
        throws Exception {
        given_anInsertedRecordWithJson(jsonDoc("user123-JSON", 44, "TX"));
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
        given_anInsertedRecordWithJson(jsonDoc("user123-JSON", 44, "TX"));
        given_theCacheIsWarmedWithTheKey("user123-JSON");
        when_removeIsCalledOnTheCacheWithKey("user123-JSON");
        then_theDatabaseContainsNullForKey("user123-JSON");
    }

    @Test
    public void ensureAJsonDocumentCanBeUpdatedInTheTableByTheCacheLoader() throws Exception {
        given_anInsertedRecordWithJson(jsonDoc("user123-JSON", 44, "TX"));
        given_theCacheIsWarmedWithTheKey("user123-JSON");
        when_putIsCalledOnTheCacheWithKeyAndValue("user123-JSON", jsonDoc("user123-JSON", 145, "MA"));
        then_theDatabaseContainsTheValueForForKey("user123-JSON", jsonDoc("user123-JSON", 145, "MA"));
    }

    @Test
    public void ensureANonExistentValueInTheDBAndTheCacheYieldsNullFromGet() throws Exception {
        when_theCacheIsQueriedForKey("NON-EXISTENT");
        then_theValueReadIs(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureAJsonMacheCannotBeInstantiatedThatIsNotOfTypeStringString() throws Exception {
        mache(String.class, Integer.class)
            .cachedBy(guava())
            .storedIn(cassandra().withCluster(theCluster())
                .withKeyspace(KEY_SPACE)
                .withSchemaOptions(CREATE_SCHEMA_IF_NEEDED)
                .asJsonDocuments()
                .inTable("users")
                .withIDField("id")
            )
            .withNoMessaging()
            .macheUp();
    }


    private void then_theDatabaseContainsTheValueForForKey(String key, String expectedJsonDoc) {
        when_theDatabaseIsQueriedForKey(key);
        assertEquals(expectedJsonDoc, this.resultFromDatabase);
    }

    private void when_putIsCalledOnTheCacheWithKeyAndValue(String key, String jsonDoc) {
        cache.put(key, jsonDoc);
    }

    private String jsonDoc(final String id, final int age, final String state) {
        return "{\"id\": \"" + id + "\", \"age\": " + age + ", \"state\": \"" + state + "\"}";
    }

    private void then_theDatabaseContainsNullForKey(String key) {
        when_theDatabaseIsQueriedForKey(key);
        assertNull(resultFromDatabase);
    }

    private void when_removeIsCalledOnTheCacheWithKey(String key) {
        this.cache.remove(key);
    }

    private void given_theCacheIsWarmedWithTheKey(String key) {
        when_theCacheIsQueriedForKey(key);
    }

    private void then_theValueRetrievedFromTheDatabaseIs(String expectedValue) {
        assertEquals(expectedValue, this.resultFromDatabase);
    }

    private void when_theDatabaseIsQueriedForKey(String key) {
        final String select = format("SELECT JSON * from %s.users where id = '%s';", KEY_SPACE, key);
        final ResultSet resultSet = getSession().execute(select);
        final Row row = resultSet.one();
        if (row != null) {
            resultFromDatabase = row.getString(0);
        } else {
            resultFromDatabase = null;
        }
    }

    private void given_TheCachePut(String key, String value) {
        cache.put(key, value);
    }

    private void then_theValueReadIs(String expectedValue) {
        assertEquals(expectedValue, cachedValueForKey);
    }

    private void when_theCacheIsQueriedForKey(String key) {
        cachedValueForKey = cache.get(key);
    }

    private void given_anInsertedRecordWithJson(String jsonValue) {
        final String insert = format("INSERT INTO %s.users JSON '%s';", KEY_SPACE, jsonValue);
        getSession().execute(insert);
    }

    private void given_anInsertedRecordWithRawColumnValues() {
        final String insert = format("INSERT INTO %s.users (id, age, state) "
            + "VALUES ('user123', 42, 'TX');", KEY_SPACE);
        getSession().execute(insert);
    }

    private Mache<String, String> exampleCache() throws Exception {
        return mache(String.class, String.class)
            .cachedBy(guava())
            .storedIn(cassandra().withCluster(theCluster())
                .withKeyspace(KEY_SPACE)
                .withSchemaOptions(CREATE_SCHEMA_IF_NEEDED)
                .asJsonDocuments()
                .inTable("users")
                .withIDField("id")
            )
            .withNoMessaging()
            .macheUp();
    }

    private Cluster.Builder theCluster() {
        return Cluster.builder()
            .withClusterName("BluePrint")
            .addContactPoint(CASSANDRA_BLUEPRINT.getHost())
            .withPort(9042);
    }

    private Session getSession() {
        final Cluster connection = connectionContext.getConnection(cache.getCacheLoader());
        return connection.connect();
    }
}
