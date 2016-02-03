package com.excelian.mache.rest;

import com.excelian.mache.core.SchemaOptions
import com.datastax.driver.core.Cluster

import java.util.concurrent.TimeUnit

import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static com.excelian.mache.builder.MacheBuilder.mache;

/**
 * Run the REST service using a Cassandra storage provider, the maps created will be evicted after 1 day.
 */
public class CassandraConfig {
    protected static String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();

    public static void main(String[] args) {
        MacheRestService restService = new MacheRestService();

        restService.runAsync({ context ->
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
                            .macheUp(), TimeUnit.DAYS.toMillis(1));
        });
    }
}