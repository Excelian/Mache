package org.mache.builder;


import org.mache.ExCache;
import org.mache.SchemaOptions;
import org.mache.builder.Builder.MacheDescriptor.StorageServerDetails;

import java.util.Arrays;

import static org.mache.builder.Builder.Storage.*;
import static org.mache.builder.Builder.Messaging.*;

/**
 * Created by jbowkett on 04/08/15.
 */
public class Builder {

  public static enum Storage {Cassandra, Mongo}

  public static enum Messaging {RabbitMQ, Kafka, None}

  public static StorageServerDetails server(String host, int port) {
    return new StorageServerDetails(host, port);
  }

  public static ClusterDetails noCluster() {
    return new IgnoredClusterDetails();
  }

  public static ClusterDetails namedCluster(String name) {
    return new ClusterDetails(name);
  }

  //rename to MacheDescriptor
  //then have a macheUp() method that will return a mache instance with the correct type info
  //the method calls out to the service interface to find the MacheFactory with the
  //correct published type and then calls into it to create the mache
  //then wires in any messaging as necessary
  //calls through to the messaging service interface to find if that's available


  static class MacheDescriptor<T> {
    public final Storage storage;
    private final StorageServerDetails[] storageServers;
    private final ClusterDetails cluster;
    private final String keyspace;
    private final Class<T> macheType;
    private final SchemaOptions schemaOption;
    private final Messaging messaging;
    private final String messagingLocation;
    private final String topic;

    private MacheDescriptor(Storage storage, StorageServerDetails[] storageServers, ClusterDetails cluster,
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
      return new StorageTypeBuilder() {
      };
    }


    public ExCache<String, T> macheUp() {
      return (ExCache<String, T>) null;
    }

    interface StorageTypeBuilder {
      default MongoServerAddressBuilder backedByMongo() {
        return (StorageServerDetails... storageServers) -> keyspace -> new MacheTypeBuilder() {
          public <T> SchemaPolicyBuilder<T> toStore(Class<T> macheType) {
            return schemaOption ->
                messaging -> messagingLocation -> topic ->
                    new MacheDescriptor<>(Mongo, storageServers, noCluster(), keyspace,
                        macheType, schemaOption, messaging, messagingLocation,
                        topic);
          }
        };
      }

      default CassandraServerAddressBuilder backedByCassandra() {
        return storageServers -> cluster ->
            keyspace -> new MacheTypeBuilder() {
              public <T> SchemaPolicyBuilder<T> toStore(Class<T> macheType) {
                return schemaOption ->
                    messaging -> messagingLocation -> topic ->
                        new MacheDescriptor<>(Cassandra, storageServers, cluster, keyspace,
                            macheType, schemaOption, messaging, messagingLocation,
                            topic);
              }
            };
      }
    }

    public interface CassandraServerAddressBuilder {
      ClusterBuilder servedFrom(StorageServerDetails... details);
    }

    public interface MongoServerAddressBuilder {
      KeyspaceBuilder at(StorageServerDetails... details);
    }

    public interface ClusterBuilder {
      KeyspaceBuilder with(ClusterDetails cluster);
    }

    public interface KeyspaceBuilder {
      MacheTypeBuilder withKeyspace(String keyspace);
    }

    public interface MacheTypeBuilder {
      <T>SchemaPolicyBuilder<T> toStore(Class<T> macheType);
    }

    public interface SchemaPolicyBuilder<T> {
      MessageQueueBuilder<T> withPolicy(SchemaOptions schemaOption);
    }

    public interface MessageQueueBuilder<T> {
      MessagingLocationBuilder<T> using(Messaging messaging);

      default MacheDescriptor<T> withNoMessaging() {
        return using(None).locatedAt("NOWHERE").listeningOnTopic("NONE");
      }
    }

    public interface MessagingLocationBuilder<T> {
      TopicBuilder<T> locatedAt(String messageServerAddress);
    }

    public interface TopicBuilder<T> {
      MacheDescriptor<T> listeningOnTopic(String topic);
    }

    public static class StorageServerDetails {
      private final String address;
      private final int port;

      private StorageServerDetails(String address, int port) {
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
  }

  private static class ClusterDetails {
    private final String name;

    public ClusterDetails(String name) {
      this.name = name;
    }
    public String toString() {
      return "Cluster['" + name + "']";
    }
  }

  private static class IgnoredClusterDetails extends ClusterDetails {
    public IgnoredClusterDetails() {
      super("");
    }
    public String toString() {
      return "NoCluster";
    }
  }
}
