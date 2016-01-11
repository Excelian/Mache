package com.excelian.mache.examples.cassandra;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;

/**
 * A factory for a Cassandra backed {@link Example}.
 */
public class CassandraExample implements Example<CassandraAnnotatedMessage> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Override
    public Mache<String, CassandraAnnotatedMessage> exampleCache() throws Exception {
        final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
        final Cluster cluster = Cluster.builder()
                .addContactPoint("10.28.1.140")
                .withPort(9042)
                .withClusterName("BluePrint").build();

        return mache(String.class, CassandraAnnotatedMessage.class)
                .backedBy(cassandra()
                        .withCluster(cluster)
                        .withKeyspace(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA).build())
                .withNoMessaging()
                .macheUp();
    }
}
