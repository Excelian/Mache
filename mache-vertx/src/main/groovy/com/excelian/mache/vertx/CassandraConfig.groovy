package com.excelian.mache.vertx;

import com.excelian.mache.builder.NoMessagingProvisioner
import com.excelian.mache.core.SchemaOptions
import com.datastax.driver.core.Cluster
import com.excelian.mache.factory.MacheFactory;

import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;


public class CassandraConfig {
    protected static String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();

    public static void main(String[] args) {
        MacheRestService restService = new MacheRestService();

        restService.run(
                new MacheFactory(
                        guava(),
                        cassandra()
                                .withCluster(Cluster.builder()
                                .withClusterName("BluePrint")
                                .addContactPoint("192.168.3.4")
                                .withPort(9042)
                                .build())
                                .withKeyspace(keySpace)
                                .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                                .build(),
                        new NoMessagingProvisioner()
                ));
    }
}