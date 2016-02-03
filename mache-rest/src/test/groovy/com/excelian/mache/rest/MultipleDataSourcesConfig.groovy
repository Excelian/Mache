package com.excelian.mache.rest

import com.datastax.driver.core.Cluster
import com.excelian.mache.core.SchemaOptions
import com.mongodb.ServerAddress

import java.util.concurrent.TimeUnit

import static com.excelian.mache.builder.MacheBuilder.mache
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;

/**
 * This configuration shows how to configure data sources dependant on the map being requested.
 *
 * For example we may wish to route all requests for trade maps such as trade-2015-01-28 and trade-2014-12-10
 * to Mongo and create the schema if it does not exist. However for all other requests we direct to Cassandra
 * and do not create any schema.
 */
public class MultipleDataSourcesConfig {
    protected static String keySpace = "NoSQL_MacheClient_Test_" + new Date().toString();

    public static void main(String[] args) {
        MacheRestService restService = new MacheRestService();

        restService.runAsync({ context ->
            if (context.getMapName().toLowerCase().startsWith("trade")) {
                new RestManagedMache(
                        mache(String.class, String.class)
                                .cachedBy(guava())
                                .storedIn(
                                mongodb()
                                        .withSeeds(new ServerAddress("localhost", 27017))
                                        .withDatabase(keySpace)
                                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                                        .asJsonDocuments()
                                        .inCollection("test"))
                                .withNoMessaging()
                                .macheUp(), TimeUnit.HOURS.toMillis(2))
            } else {
                new RestManagedMache(
                        mache(String.class, String.class)
                                .cachedBy(guava())
                                .storedIn(
                                cassandra()
                                        .withCluster(Cluster.builder()
                                            .withClusterName("BluePrint")
                                            .addContactPoint("192.168.3.4")
                                            .withPort(9042))
                                        .withKeyspace(keySpace)
                                        .withSchemaOptions(SchemaOptions.USE_EXISTING_SCHEMA)
                                        .asJsonDocuments()
                                        .inTable("names")
                                        .withIDField("id"))
                                .withNoMessaging()
                                .macheUp(), 0)
            }
        });
    }
}