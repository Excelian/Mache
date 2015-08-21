package com.excelian.mache.builder;

import static com.excelian.mache.builder.Builder.Messaging.None;
import static com.excelian.mache.builder.Builder.Storage.Cassandra;
import static com.excelian.mache.builder.Builder.Storage.Mongo;

import com.excelian.mache.builder.Builder.MacheDescriptor.StorageTypeBuilder;
import com.excelian.mache.builder.StorageProvisioner.ClusterDetails;
import com.excelian.mache.builder.StorageProvisioner.IgnoredClusterDetails;
import com.excelian.mache.builder.StorageProvisioner.StorageServerDetails;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.observable.MessageQueueObservableCacheFactory;
import com.excelian.mache.observable.builder.MessagingProvisioner;
import com.excelian.mache.observable.utils.UUIDUtils;


import java.io.IOException;
import java.util.ServiceLoader;
import javax.jms.JMSException;


/**
 * Created by jbowkett on 04/08/15.
 */
public class Builder {
    public static StorageServerDetails server(String host, int port) {
        return new StorageServerDetails(host, port);
    }

    public static ClusterDetails noCluster() {
        return new IgnoredClusterDetails();
    }

    public static ClusterDetails namedCluster(String name) {
        return new ClusterDetails(name);
    }

    public static StorageTypeBuilder mache() {
        return new StorageTypeBuilder() {
        };
    }

    enum Storage {Cassandra, Mongo}

    enum Messaging {ActiveMQ, RabbitMQ, Kafka, None}

    public static class MacheDescriptor<T> {
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


        /**
         * Returns a mache instance with the correct type info.
         * Calls out to the service interface to find the StorageProvisioner with the
         * correct published type and then calls into it to create the mache
         * then wires in any messaging as necessary
         * calls through to the messaging service interface to find if that's available
         *
         * @return a cache mapping strings to the given type, using the options
         * specified in the constructor
         */
        public Mache<String, T> macheUp() {
            final StorageProvisioner storageProvisioner = getStorageProvisionerOrThrow();

            final Mache<String, T> cache = storageProvisioner.getCache(
                this.keyspace, macheType, this.schemaOption, this.cluster, this.storageServers);

            if (this.messaging != None) {
                final MessagingProvisioner messagingProvisioner = getMessagingProvisionerOrThrow();
                final MQFactory mqFactory = getMqFactoryOrThrowRuntimeException(messagingProvisioner);
                final MQConfiguration mqConfiguration = () -> this.topic;

                final MacheFactory macheFactory = new MacheFactory();

                final MessageQueueObservableCacheFactory cacheFactory = new MessageQueueObservableCacheFactory(
                    mqFactory, mqConfiguration, macheFactory, new UUIDUtils());
                return cacheFactory.createCache(cache);
            }
            return cache;
        }

        private MQFactory getMqFactoryOrThrowRuntimeException(MessagingProvisioner messagingProvisioner) {
            try {
                return messagingProvisioner.getMQFactory(this.messagingLocation);
            }
            catch (IOException | JMSException e) {
                throw new RuntimeException("Cannot connect to message queue at:[" + this.messagingLocation + "]", e);
            }
        }

        private StorageProvisioner getStorageProvisionerOrThrow() {
            final ServiceLoader<StorageProvisioner> allInClasspath = ServiceLoader.load(StorageProvisioner.class);
            StorageProvisioner provisioner = null;
            for (StorageProvisioner storageProvisioner : allInClasspath) {
                if (this.storage.name().equalsIgnoreCase(storageProvisioner.getStorage())) {
                    provisioner = storageProvisioner;
                }
            }
            if (provisioner == null) {
                throw new RuntimeException("Cannot find storage provisioner for platform :[" +
                    storage + "].  Please ensure a " + storage + "-ns.jar is present in the classpath");
            }
            return provisioner;
        }

        private MessagingProvisioner getMessagingProvisionerOrThrow() {
            final ServiceLoader<MessagingProvisioner> allInClasspath = ServiceLoader.load(MessagingProvisioner.class);
            MessagingProvisioner provisioner = null;
            for (MessagingProvisioner messagingProvisioner : allInClasspath) {
                if (this.messaging.name().equalsIgnoreCase(messagingProvisioner.getMessaging())) {
                    provisioner = messagingProvisioner;
                }
            }
            if (provisioner == null) {
                throw new RuntimeException("Cannot find messaging provisioner for platform :[" +
                    messaging + "].  Please ensure the core-observable.jar is present in the classpath");
            }
            return provisioner;
        }

        public interface StorageTypeBuilder {
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
                return storageServer -> cluster ->
                    keyspace -> new MacheTypeBuilder() {
                        public <T> SchemaPolicyBuilder<T> toStore(Class<T> macheType) {
                            return schemaOption ->
                                messaging -> messagingLocation -> topic ->
                                    new MacheDescriptor<>(Cassandra,
                                        new StorageServerDetails[]{storageServer},
                                        cluster, keyspace, macheType, schemaOption,
                                        messaging, messagingLocation, topic);
                        }
                    };
            }
        }

        public interface CassandraServerAddressBuilder {
            ClusterBuilder at(StorageServerDetails details);
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
            <T> SchemaPolicyBuilder<T> toStore(Class<T> macheType);
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
    }
}
