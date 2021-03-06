package com.excelian.mache.examples.cassandra;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;

/**
 * A factory for a Cassandra backed {@link Example}.
 */
public class CassandraExample implements Example<CassandraAnnotatedMessage, CassandraAnnotatedMessage> {

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private String serverIpAddress;

    public CassandraExample(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }


    @Override
    public Mache<String, CassandraAnnotatedMessage> exampleCache() throws Exception {
        final String keySpace = "NoSQL_MacheClient_Test_" + dateFormat.format(new Date());

        return mache(String.class, CassandraAnnotatedMessage.class)
                .cachedBy(guava())
                .storedIn(cassandra()
                    .withCluster(Cluster.builder()
                        .addContactPoint(serverIpAddress)
                        .withPort(9042)
                        .withClusterName("BluePrint"))
                        .withKeyspace(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA).build())
                .withNoMessaging()
                .macheUp();
    }

    @Override
    public CassandraAnnotatedMessage createEntity(String primaryKey, String msg) {
        return new CassandraAnnotatedMessage(primaryKey, msg);
    }
}
