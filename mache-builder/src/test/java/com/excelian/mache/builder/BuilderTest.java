package com.excelian.mache.builder;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.NoRunningCassandraDbForTests;
import com.excelian.mache.core.NoRunningCouchbaseDbForTests;
import com.excelian.mache.core.NoRunningMongoDbForTests;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.cassandra.CassandraAnnotatedMessage;
import com.excelian.mache.examples.couchbase.CouchbaseAnnotatedMessage;
import com.excelian.mache.examples.mongo.MongoAnnotatedMessage;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class BuilderTest {
    private static String CASSANDRA_HOST;
    private static int CASSANDRA_PORT;
    private static int MONGO_PORT;
    private static String MONGO_HOST;
    private static String COUCHBASE_HOST;
    private static int COUCHBASE_PORT;

    //can test for the constraints and also that the annotations are inspected
    //this is a mache descriptor

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @BeforeClass
    public static void initServerAddressesOnLoadingTestClass() {
        CASSANDRA_HOST = new NoRunningCassandraDbForTests().getHost();
        CASSANDRA_PORT = 9042;
        MONGO_HOST = new NoRunningMongoDbForTests().getHost();
        MONGO_PORT = 27017;
        COUCHBASE_HOST = new NoRunningCouchbaseDbForTests().getHost();
        COUCHBASE_PORT = 8091;
    }

    @Test
    @IgnoreIf(condition = NoRunningCassandraDbForTests.class)
    public void testAMacheCanBeCreatedBackedByCassandraWithANamedClusterAndRabbitForMessaging() {
        final Mache<String, CassandraAnnotatedMessage> mache =
                Builder.mache()
                        .backedByCassandra()
                        .at(Builder.server(CASSANDRA_HOST, CASSANDRA_PORT))
                        .with(Builder.namedCluster("Blueprint"))
                        .withKeyspace("Keyspace")
                        .toStore(CassandraAnnotatedMessage.class)
                        .withPolicy(SchemaOptions.CREATEANDDROPSCHEMA)
                        .using(Builder.Messaging.RabbitMQ)
                        .locatedAt("localhost")
                        .listeningOnTopic("TRADES")
                        .macheUp();
        assertNotNull(mache);
    }

    @Test
    @IgnoreIf(condition = NoRunningCouchbaseDbForTests.class)
    public void testAMacheCanBeCreatedBackedByCouchbaseWithANamedClusterAndRabbitForMessaging() {
        final Mache<String, CouchbaseAnnotatedMessage> mache =
                Builder.mache()
                        .backedByCouchbase()
                        .at(Builder.server(COUCHBASE_HOST, COUCHBASE_PORT))
                        .with(Builder.namedCluster("Blueprint"))
                        .withKeyspace("Keyspace")
                        .toStore(CouchbaseAnnotatedMessage.class)
                        .withPolicy(SchemaOptions.CREATEANDDROPSCHEMA)
                        .using(Builder.Messaging.RabbitMQ)
                        .locatedAt("localhost")
                        .listeningOnTopic("TRADES")
                        .macheUp();
        assertNotNull(mache);
    }

    @Test
    @IgnoreIf(condition = NoRunningCassandraDbForTests.class)
    public void testAMacheCanBeCreatedBackedByCassandraWithANamedClusterAndNoMessaging() {
        final Mache<String, CassandraAnnotatedMessage> mache =
                Builder.mache()
                        .backedByCassandra()
                        .at(Builder.server(CASSANDRA_HOST, CASSANDRA_PORT))
                        .with(Builder.namedCluster("Blueprint"))
                        .withKeyspace("Keyspace")
                        .toStore(CassandraAnnotatedMessage.class)
                        .withPolicy(SchemaOptions.CREATEANDDROPSCHEMA)
                        .withNoMessaging()
                        .macheUp();
        assertNotNull(mache);
    }

    @Test
    @IgnoreIf(condition = NoRunningMongoDbForTests.class)
    public void testAMacheCanBeCreatedBackedByMongoWithRabbitForMessaging() {
        final Mache<String, MongoAnnotatedMessage> mache =
                Builder.mache()
                        .backedByMongo()
                        .at(Builder.server(MONGO_HOST, MONGO_PORT))
                        .withKeyspace("Keyspace")
                        .toStore(MongoAnnotatedMessage.class)
                        .withPolicy(SchemaOptions.CREATEANDDROPSCHEMA)
                        .using(Builder.Messaging.RabbitMQ)
                        .locatedAt("localhost")
                        .listeningOnTopic("TRADES")
                        .macheUp();
        assertNotNull(mache);
    }

    @Test
    @IgnoreIf(condition = NoRunningMongoDbForTests.class)
    public void testAMongoCacheCanBeCreatedWithMultipleServers() {
        final Mache<String, MongoAnnotatedMessage> mache =
                Builder.mache()
                        .backedByMongo()
                        .at(Builder.server(MONGO_HOST, MONGO_PORT), Builder.server(MONGO_HOST, MONGO_PORT))
                        .withKeyspace("Keyspace")
                        .toStore(MongoAnnotatedMessage.class)
                        .withPolicy(SchemaOptions.CREATEANDDROPSCHEMA)
                        .withNoMessaging()
                        .macheUp();
        assertNotNull(mache);
    }

    @Test
    @IgnoreIf(condition = NoRunningMongoDbForTests.class)
    public void testAMongoCacheCanBeCreatedWithoutMessaging() {
        final StorageProvisioner.StorageServerDetails dbServer = Builder.server(MONGO_HOST, MONGO_PORT);
        final Mache<String, Value> mache =
                Builder.mache().backedByMongo()
                        .at(dbServer)
                        .withKeyspace("Keyspace")
                        .toStore(Value.class)
                        .withPolicy(SchemaOptions.CREATEANDDROPSCHEMA)
                        .withNoMessaging()
                        .macheUp();
        assertNotNull(mache);
    }

    static class Value {
    }
}
