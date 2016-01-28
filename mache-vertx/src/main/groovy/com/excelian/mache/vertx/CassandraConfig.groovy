package com.excelian.mache.vertx;

import com.excelian.mache.core.SchemaOptions
import com.datastax.driver.core.Cluster

import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static com.excelian.mache.builder.MacheBuilder.mache;

public class CassandraConfig {
    protected static String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();

    public static void main(String[] args) {
        MacheRestService restService = new MacheRestService();

        restService.runAsync({ context ->
            mache(String.class, String.class)
                    .cachedBy(guava())
                    .storedIn(
                    cassandra()
                            .withCluster(Cluster.builder()
                            .withClusterName("BluePrint")
                            .addContactPoint("192.168.3.4")
                            .withPort(9042))
                            .withKeyspace(keySpace)
                            .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                            .asJsonDocuments()
                            .inTable("names")
                            .withIDField("id"))
                    .withNoMessaging()
                    .macheUp();
        });
    }
}