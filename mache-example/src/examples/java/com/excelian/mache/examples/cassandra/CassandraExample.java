package com.excelian.mache.examples.cassandra;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandraConnectionContext;

/**
 * A factory for a Cassandra backed {@link Example}.
 */
public class CassandraExample implements Example<CassandraAnnotatedMessage, Cluster, CassandraAnnotatedMessage> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private String serverIpAdress;

    public CassandraExample(String serverIpAdress)
    {
        this.serverIpAdress = serverIpAdress;
    }

    public ConnectionContext<Cluster> createConnectionContext()
    {
        return cassandraConnectionContext(Cluster.builder()
                .addContactPoint(serverIpAdress)
                .withPort(9042)
                .withClusterName("BluePrint"));
    }

    @Override
    public Mache<String, CassandraAnnotatedMessage> exampleCache(ConnectionContext<Cluster> connectionContext) throws Exception {
        final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());

        return mache(String.class, CassandraAnnotatedMessage.class)
                .backedBy(cassandra()
                        .withContext(connectionContext)
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
