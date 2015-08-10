package org.mache.builder;

import org.junit.Test;
import org.mache.ExCache;
import org.mache.examples.cassandra.CassandraAnnotatedMessage;
import org.mache.examples.mongo.MongoAnnotatedMessage;

import static org.junit.Assert.assertNotNull;
import static org.mache.SchemaOptions.CREATEANDDROPSCHEMA;
import static org.mache.builder.Builder.*;
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

  @Test
  public void testAMacheCanBeCreatedBackedByCassandraWithANamedClusterAndRabbitForMessaging() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
        mache()
            .backedByCassandra()
            .servedFrom(server("10.28.1.140", 27017))
            .with(namedCluster("Blueprint"))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .using(RabbitMQ)
            .locatedAt("localhost")
            .listeningOnTopic("TRADES")
        .macheUp();

    System.out.println("mache = " + mache);
  }

  @Test
  public void testAMacheCanBeCreatedBackedByCassandraWithANamedClusterAndNoMessaging() {
    final ExCache<String, CassandraAnnotatedMessage> mache =
        mache()
            .backedByCassandra()
            .servedFrom(server("10.28.1.140", 27017))
            .with(namedCluster("Blueprint"))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .withNoMessaging()
            .macheUp();

    System.out.println("mache = " + mache);
  }

  @Test
  public void testAMacheCanBeCreatedBackedByMongoWithRabbitForMessaging() {
    final ExCache<String, CassandraAnnotatedMessage> mache2 =
        mache()
            .backedByMongo()
            .servedFrom(server("10.28.1.140", 27017))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .using(RabbitMQ)
            .locatedAt("localhost")
            .listeningOnTopic("TRADES")
            .macheUp();
    System.out.println("mache2 = " + mache2);
  }

  @Test
  public void testAMongoCacheCanBeCreatedWithMultipleServers() {
    final ExCache<String, CassandraAnnotatedMessage> mache3 =
        mache()
            .backedByMongo()
            .servedFrom(server("10.28.1.140", 27017), server("10.28.1.140", 27017))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .withNoMessaging()
            .macheUp();

    assertNotNull(mache3);
    System.out.println("mache3 = " + mache3);
  }

  @Test
  public void testAMongoCacheCanBeCreatedWithoutMessaging() {
    final ExCache<String, MongoAnnotatedMessage> mache4 =
        mache()
            .backedByMongo()
            .servedFrom(server("10.28.1.140", 27017))
            .withKeyspace("Keyspace")
            .toStore(MongoAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .withNoMessaging()
            .macheUp();

    System.out.println("mache4 = " + mache4);
  }
}
