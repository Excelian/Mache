package org.mache.builder;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import org.junit.Rule;
import org.junit.Test;
import org.mache.ExCache;
import org.mache.NoRunningCassandraDbForTests;
import org.mache.builder.StorageProvisioner.StorageServerDetails;
import org.mache.examples.cassandra.CassandraAnnotatedMessage;

import static org.junit.Assert.assertNotNull;
import static org.mache.SchemaOptions.CREATEANDDROPSCHEMA;
import static org.mache.builder.Builder.MacheDescriptor.mache;
import static org.mache.builder.Builder.Messaging.RabbitMQ;
import static org.mache.builder.Builder.namedCluster;
import static org.mache.builder.Builder.server;

/**
 * Created by jbowkett on 10/08/15.
 */
public class BuilderTest {

  //can test for the constraints and also that the annotations are inspected
  //this is a mache descriptor

  @Rule
  public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

  @Test
  @IgnoreIf(condition = NoRunningCassandraDbForTests.class)
  public void testAMacheCanBeCreatedBackedByCassandraWithANamedClusterAndRabbitForMessaging() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
        mache()
            .backedByCassandra()
            .at(server("10.28.1.140", 27017))
            .with(namedCluster("Blueprint"))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .using(RabbitMQ)
            .locatedAt("localhost")
            .listeningOnTopic("TRADES")
        .macheUp();
     assertNotNull(mache);
  }

  @Test
  @IgnoreIf(condition = NoRunningCassandraDbForTests.class)
  public void testAMacheCanBeCreatedBackedByCassandraWithANamedClusterAndNoMessaging() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
        mache()
            .backedByCassandra()
            .at(server("10.28.1.140", 27017))
            .with(namedCluster("Blueprint"))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .withNoMessaging()
            .macheUp();
    assertNotNull(mache);
  }

  @Test
  public void testAMacheCanBeCreatedBackedByMongoWithRabbitForMessaging() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
        mache()
            .backedByMongo()
            .at(server("10.28.1.140", 27017))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .using(RabbitMQ)
            .locatedAt("localhost")
            .listeningOnTopic("TRADES")
            .macheUp();
    assertNotNull(mache);
  }

  @Test
  public void testAMongoCacheCanBeCreatedWithMultipleServers() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
        mache()
            .backedByMongo()
            .at(server("10.28.1.140", 27017), server("10.28.1.140", 27017))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .withNoMessaging()
            .macheUp();
    assertNotNull(mache);
  }

  @Test
  public void testAMongoCacheCanBeCreatedWithoutMessaging() {
    final StorageServerDetails dbServer = server("10.28.1.140", 27017);
    final ExCache<String, Value> mache =
mache().backedByMongo().at(dbServer).withKeyspace("Kspace").toStore(Value.class).withPolicy(CREATEANDDROPSCHEMA).withNoMessaging().macheUp();
    assertNotNull(mache);
  }
  static class Value{}
}
