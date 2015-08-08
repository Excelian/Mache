package org.mache.builder;


import org.mache.SchemaOptions;
import org.mache.examples.cassandra.CassandraAnnotatedMessage;

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
            .servedFrom("10.28.1.140", 27017)
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
            .servedFrom("10.28.1.140", 27017)
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
            .servedFrom("10.28.1.140", 27017)
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .withNoMessaging();

    System.out.println("mache3 = " + mache3);

    @SuppressWarnings("unchecked")
    final Mache<CassandraAnnotatedMessage> mache4 =
        mache()
            .backedByMongo()
            .servedFrom("10.28.1.140", 27017)
            .withKeyspace("Keyspace")
            .toStore(CassandraAnnotatedMessage.class)
            .withPolicy(CREATEANDDROPSCHEMA)
            .withNoMessaging();

    System.out.println("mache4 = " + mache4);

  }


  private static ClusterDetails noCluster() {
    return new IgnoredClusterDetails();
  }

  private static ClusterDetails namedCluster(String name) {
    return new ClusterDetails(name);
  }


  static class Mache<T> {
    public final Storage storage;
    public final String ipAddress;
    private final int port;
    private final ClusterDetails cluster;
    private final String keyspace;
    private final Class<T> macheType;
    private final SchemaOptions schemaOption;
    private final Messaging messaging;
    private final String messagingLocation;
    private final String topic;

    private Mache(Storage storage, String ipAddress, int port, ClusterDetails cluster,
                  String keyspace, Class<T> macheType, SchemaOptions schemaOption,
                  Messaging messaging, String messagingLocation, String topic) {
      this.storage = storage;
      this.ipAddress = ipAddress;
      this.port = port;
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
      final StringBuilder sb = new StringBuilder("Mache{");
      sb.append("storage=").append(storage);
      sb.append(", ipAddress='").append(ipAddress).append('\'');
      sb.append(", port=").append(port);
      sb.append(", cluster='").append(cluster).append('\'');
      sb.append(", keyspace='").append(keyspace).append('\'');
      sb.append(", macheType=").append(macheType.getSimpleName());
      sb.append(", schemaOption=").append(schemaOption);
      sb.append(", messaging=").append(messaging);
      sb.append(", messagingLocation='").append(messagingLocation).append('\'');
      sb.append(", topic='").append(topic).append('\'');
      sb.append('}');
      return sb.toString();
    }


    public static StorageTypeBuilder mache() {
      return new StorageTypeBuilder(){};
    }

//    public static StorageTypeBuilder person2() {
//      return storage -> (ipAddress, port) -> cluster -> keyspace ->
//          macheType -> schemaOption -> messaging -> messagingLocation ->
//              topic ->
//                  new Mache(storage, ipAddress, port, cluster, keyspace,
//                      macheType, schemaOption, messaging, messagingLocation,
//                      topic);
//    }

    @SuppressWarnings("unchecked")
    public Mache<T> toMache() {
      return (Mache<T>)this;
    }
  }

  interface StorageTypeBuilder {
    default MongoServerAddressBuilder backedByMongo(){
      return (ipAddress, port) ->
          keyspace -> macheType -> schemaOption ->
          messaging -> messagingLocation -> topic ->
              new Mache(Mongo, ipAddress, port, noCluster(), keyspace,
                            macheType, schemaOption, messaging, messagingLocation,
                            topic);
    }

    default CassandraServerAddressBuilder backedByCassandra(){
      return (ipAddress, port) -> cluster ->
          keyspace -> macheType -> schemaOption ->
          messaging -> messagingLocation -> topic ->
              new Mache(Cassandra, ipAddress, port, cluster, keyspace,
                            macheType, schemaOption, messaging, messagingLocation,
                            topic);
    }
  }

  interface CassandraServerAddressBuilder {
    ClusterBuilder servedFrom(String ipAddress, int port);
  }

  interface MongoServerAddressBuilder {
    KeyspaceBuilder servedFrom(String ipAddress, int port);
  }

  interface ClusterBuilder {
    KeyspaceBuilder with(ClusterDetails cluster);

    default KeyspaceBuilder withNoCluster(){
      return with(new IgnoredClusterDetails());
    }
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
