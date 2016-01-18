package com.excelian.mache.vertx;

import com.excelian.mache.builder.NoMessagingProvisioner
import com.excelian.mache.core.SchemaOptions
import com.excelian.mache.core.Mache;
import com.excelian.mache.vertx.MacheVertical;
import com.datastax.driver.core.Cluster;

import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;


public class CassandraConfig {
    protected static String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();

    public static void main(String[] args) {
        MacheVertical vertical = new MacheVertical(
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
        );

        vertical.run();
    }
}