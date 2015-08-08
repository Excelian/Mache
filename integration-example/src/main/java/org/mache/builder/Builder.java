package org.mache.builder;


import org.mache.SchemaOptions;
import org.mache.examples.cassandra.CassandraAnnotatedMessage;

import java.util.Arrays;

import static org.mache.SchemaOptions.*;
import static org.mache.builder.Builder.Mache.mache;
import static org.mache.builder.Builder.Storage.*;
import static org.mache.builder.Builder.Messaging.*;

/**
 * Created by jbowkett on 04/08/15.
 */
public class Builder {

  public static enum Storage{Cassandra, Mongo}
  public static enum Messaging{RabbitMQ, Kafka, None}

  public static void main(String...args) {
    final Mache mache =
        mache()
            .backedByCassandra()
            .servedFrom(server("10.28.1.140", 27017))
            .with(namedCluster("Blueprint"))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .using(RabbitMQ)
            .locatedAt("localhost")
            .listeningOnTopic("TRADES");

    System.out.println("mache = " + mache);

    @SuppressWarnings("unchecked")
    final Mache<CassandraAnnotatedMessage> mache2 =
        mache()
            .backedByMongo()
            .servedFrom(server("10.28.1.140", 27017))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .using(RabbitMQ)
            .locatedAt("localhost")
            .listeningOnTopic("TRADES");
    System.out.println("mache2 = " + mache2);

    @SuppressWarnings("unchecked")
    final Mache<CassandraAnnotatedMessage> mache3 =
        mache()
            .backedByMongo()
            .servedFrom(server("10.28.1.140", 27017), server("10.28.1.140", 27017))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .withNoMessaging();

    System.out.println("mache3 = " + mache3);

    @SuppressWarnings("unchecked")
    final Mache<CassandraAnnotatedMessage> mache4 =
        mache()
            .backedByMongo()
            .servedFrom(server("10.28.1.140", 27017))
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .withNoMessaging();

    System.out.println("mache4 = " + mache4);

  }

  private static StorageServerDetails server(String host, int port) {
    return new StorageServerDetails(host, port);
  }


  private static ClusterDetails noCluster() {
    return new IgnoredClusterDetails();
  }

  private static ClusterDetails namedCluster(String name) {
    return new ClusterDetails(name);
  }


  static class Mache<T> {
    public final Storage storage;
    private final StorageServerDetails [] storageServers;
    private final ClusterDetails cluster;
    private final String keyspace;
    private final Class<T> macheType;
    private final SchemaOptions schemaOption;
    private final Messaging messaging;
    private final String messagingLocation;
    private final String topic;

    private Mache(Storage storage, StorageServerDetails[] storageServers, ClusterDetails cluster,
                  String keyspace, Class<T> macheType, SchemaOptions schemaOption,
                  Messaging messaging, String messagingLocation, String topic) {
      this.storage = storage;
      this.storageServers = storageServers;
      this.cluster = cluster;
      this.keyspace = keyspace;
      this.macheType = macheType;
      this.schemaOption = schemaOption;
      this.messaging = messaging;
      this.messagingLocation = messagingLocation;
      this.topic = topic;
    }


    @Override
    public String toString() {
      return "Mache{" +
          "storage=" + storage +
          ", storageServers=" + Arrays.toString(storageServers) +
          ", cluster=" + cluster +
          ", keyspace='" + keyspace + '\'' +
          ", macheType=" + macheType +
          ", schemaOption=" + schemaOption +
          ", messaging=" + messaging +
          ", messagingLocation='" + messagingLocation + '\'' +
          ", topic='" + topic + '\'' +
          '}';
    }

    public static StorageTypeBuilder mache() {
      return new StorageTypeBuilder(){};
    }


    @SuppressWarnings("unchecked")
    public Mache<T> toMache() {
      return (Mache<T>)this;
    }
  }

  interface StorageTypeBuilder {
    default MongoServerAddressBuilder backedByMongo(){
      return storageServers ->
          keyspace -> macheType -> schemaOption ->
          messaging -> messagingLocation -> topic ->
              new Mache(Mongo, storageServers, noCluster(), keyspace,
                            macheType, schemaOption, messaging, messagingLocation,
                            topic);
    }

    default CassandraServerAddressBuilder backedByCassandra(){
      return storageServers -> cluster ->
          keyspace -> macheType -> schemaOption ->
          messaging -> messagingLocation -> topic ->
              new Mache(Cassandra, storageServers, cluster, keyspace,
                            macheType, schemaOption, messaging, messagingLocation,
                            topic);
    }
  }

  interface CassandraServerAddressBuilder {
    ClusterBuilder servedFrom(StorageServerDetails...details);
  }

  interface MongoServerAddressBuilder {
    KeyspaceBuilder servedFrom(StorageServerDetails...details);
  }

  interface ClusterBuilder {
    KeyspaceBuilder with(ClusterDetails cluster);
  }

  interface KeyspaceBuilder {
    MacheTypeBuilder withKeyspace(String keyspace);
  }

  interface MacheTypeBuilder {
    SchemaPolicyBuilder toStore(Class<?> macheType);
  }

  interface SchemaPolicyBuilder {
    MessageQueueBuilder withPolicy(SchemaOptions schemaOption);
  }

  interface MessageQueueBuilder {
    MessagingLocationBuilder using(Messaging messaging);

    default Mache withNoMessaging(){
       return using(None).locatedAt("NOWHERE").listeningOnTopic("NONE");
    }
  }

  interface MessagingLocationBuilder {
    TopicBuilder locatedAt(String messageServerAddress);
  }

  interface TopicBuilder {
    Mache listeningOnTopic(String topic);
  }

  private static class StorageServerDetails{
    private final String address;
    private final int port;

    private StorageServerDetails(String address, int port){
      this.address = address;
      this.port = port;
    }

    @Override
    public String toString() {
      return "StorageServerDetails{" +
          "address='" + address + '\'' +
          ", port=" + port +
          '}';
    }
  }


  private static class ClusterDetails {
    private final String name;

    public ClusterDetails(String name) {
      this.name = name;
    }
    public String toString(){
      return "Cluster['"+name+"']";
    }
  }

  private static class IgnoredClusterDetails extends ClusterDetails {
    public IgnoredClusterDetails() {
      super("");
    }
    public String toString(){
      return "NoCluster";
    }
  }
}
