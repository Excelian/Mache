package com.excelian.mache.examples.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.couchbase.client.java.cluster.DefaultBucketSettings.builder;
import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.couchbase.builder.CouchbaseProvisioner.couchbase;
import static com.excelian.mache.couchbase.builder.CouchbaseProvisioner.couchbaseConnectionContext;

/**
 * A factory for a Couchbase backed {@link Example}.
 */
public class CouchbaseExample implements Example<CouchbaseAnnotatedMessage, Cluster, CouchbaseAnnotatedMessage> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private String serverIpAdress;

    public CouchbaseExample(String serverIpAddress)
    {
        this.serverIpAdress = serverIpAddress;
    }

    @Override
    public Mache<String, CouchbaseAnnotatedMessage> exampleCache(ConnectionContext<Cluster> context) throws Exception {

        final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());

        return mache(String.class, CouchbaseAnnotatedMessage.class)
                .backedBy(couchbase()
                        .withContext(context)
                        .withBucketSettings(builder().name(keySpace).quota(150).build())
                        .withDefaultAdminDetails()
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build())
                .withNoMessaging()
                .macheUp();
    }

    @Override
    public CouchbaseAnnotatedMessage createEntity(String primaryKey, String msg) {
        return new CouchbaseAnnotatedMessage(primaryKey, msg);
    }

    @Override
    public ConnectionContext<Cluster> createConnectionContext() {
        return couchbaseConnectionContext(serverIpAdress, DefaultCouchbaseEnvironment.create());
    }
}
