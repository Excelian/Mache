package com.excelian.mache.cassandra.builder;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.excelian.mache.cassandra.NoRunningCassandraDbForTests;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraConnectionContext.getInstance;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.core.SchemaOptions.CREATE_SCHEMA_IF_NEEDED;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static org.junit.Assert.assertEquals;

@ConditionalIgnoreRule.IgnoreIf(condition = NoRunningCassandraDbForTests.class)
public class CassandraJsonCacheLoaderIntegrationTest {

    private static final NoRunningCassandraDbForTests CASSANDRA_BLUEPRINT = new NoRunningCassandraDbForTests();

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private CassandraConnectionContext connectionContext;
    private Mache<String, String> cache;
    private String cachedValueForKey;
    private String resultFromDatabase;

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
        return "CREATE TABLE if not exists users ("
            + "id text PRIMARY KEY, "
            + "age int, "
            + "state text)";
    }

    @After
    public void executeAfterEachTest() {
        getSession().execute(dropTable());
        if (cache != null) {
            cache.close();
        }
    }

    private String dropTable() {
        return "DROP TABLE if exists users";
    }

    @Test
    public void ensureAJsonDocumentCanBeReadAsJsonFromExistingRecordsWhenThatRecordWasNotInsertedAsJson()
        throws Exception {
        given_anInsertedRecordWithRawColumnValues();
        when_theCacheIsQueriedForKey("user123");
        then_theValueReadIs("{\"id\": \"user123\", \"age\": 42, \"state\": \"TX\"}");
    }

    @Test
    public void ensureAJsonDocumentCanBeReadAsJsonFromExistingRecordsWhenThatRecordWasInsertedAsJson()
        throws Exception {
        given_anInsertedRecordWithJsonValues();
        when_theCacheIsQueriedForKey("user123-JSON");
        then_theValueReadIs("{\"id\": \"user123-JSON\", \"age\": 44, \"state\": \"TX\"}");
    }

    @Test
    public void ensureAJsonDocumentCanBeWrittenBackToTheTableFromTheCache() throws Exception {
        final String jsonDocValue = "{\"id\": \'new-key-123\', \"age\": 99, \"state\": \"MA\"}";
        given_TheCachePut("new-key-123", jsonDocValue);
        when_theDatabaseIsQueriedForKey("new-key-123");
        then_theValueRetrievedFromTheDatabaseIs(jsonDocValue);
    }

    private void then_theValueRetrievedFromTheDatabaseIs(String expectedValue) {
        assertEquals(expectedValue, this.resultFromDatabase);
    }

    private void when_theDatabaseIsQueriedForKey(String key) {
        final ResultSet resultSet = getSession().execute("SELECT JSON * from users where id = '" + key + "'");
        resultFromDatabase = resultSet.one().getString(0);
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

    private void given_anInsertedRecordWithJsonValues() {
        final String jsonValue = "'{\"id\": \"user123-JSON\", \"age\": 44, \"state\": \"TX\"}'";
        final String insert = "INSERT INTO users JSON " + jsonValue;
        getSession().execute(insert);
    }

    private void given_anInsertedRecordWithRawColumnValues() {
        getSession().execute("INSERT INTO users (id, age, state) VALUES ('user123', 42, 'TX')");
    }

    private Mache<String, String> exampleCache() throws Exception {
        final String keySpace = "mache-json";
        return mache(String.class, String.class)
            .cachedBy(guava())
            .storedIn(cassandra().withCluster(theCluster())
                .withKeyspace(keySpace)
                .withSchemaOptions(CREATE_SCHEMA_IF_NEEDED)
                .asJsonDocuments()
                .inTable("users")
                .withIDField("id")
                .build())
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
