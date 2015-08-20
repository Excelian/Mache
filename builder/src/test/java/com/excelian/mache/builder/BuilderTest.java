package com.excelian.mache.builder;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.excelian.mache.ExCache;
import com.excelian.mache.NoRunningCassandraDbForTests;
import com.excelian.mache.NoRunningMongoDbForTests;
import com.excelian.mache.SchemaOptions;
import com.excelian.mache.examples.cassandra.CassandraAnnotatedMessage;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Created by jbowkett on 10/08/15.
 */
public class BuilderTest {
  private static String CASSANDRA_HOST;
  private static int CASSANDRA_PORT;
  private static int MONGO_PORT;
  private static String MONGO_HOST;

  //can test for the constraints and also that the annotations are inspected
  //this is a mache descriptor

  @Rule
  public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

  @BeforeClass
  public static void initServerAddressesOnLoadingTestClass(){
    CASSANDRA_HOST = new NoRunningCassandraDbForTests().HostName();
    CASSANDRA_PORT = 9042;
    MONGO_HOST = new NoRunningMongoDbForTests().HostName();
    MONGO_PORT = 27017;
  }

  @Test
  @IgnoreIf(condition = NoRunningCassandraDbForTests.class)
  public void testAMacheCanBeCreatedBackedByCassandraWithANamedClusterAndRabbitForMessaging() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
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
  @IgnoreIf(condition = NoRunningCassandraDbForTests.class)
  public void testAMacheCanBeCreatedBackedByCassandraWithANamedClusterAndNoMessaging() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
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
  public void testAMacheCanBeCreatedBackedByMongoWithRabbitForMessaging() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
        Builder.mache()
            .backedByMongo()
            .at(Builder.server(MONGO_HOST, MONGO_PORT))
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
  public void testAMongoCacheCanBeCreatedWithMultipleServers() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
        Builder.mache()
            .backedByMongo()
            .at(Builder.server(MONGO_HOST, MONGO_PORT), Builder.server(MONGO_HOST, MONGO_PORT))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(SchemaOptions.CREATEANDDROPSCHEMA)
            .withNoMessaging()
            .macheUp();
    assertNotNull(mache);
  }

  @Test
  public void testAMongoCacheCanBeCreatedWithoutMessaging() {
    final StorageProvisioner.StorageServerDetails dbServer = Builder.server(MONGO_HOST, MONGO_PORT);
    final ExCache<String, Value> mache =
Builder.mache().backedByMongo().at(dbServer).withKeyspace("Kspace").toStore(Value.class).withPolicy(SchemaOptions.CREATEANDDROPSCHEMA).withNoMessaging().macheUp();
    assertNotNull(mache);
  }
  static class Value{}
}
