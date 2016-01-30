package com.excelian.mache.examples.couchbase;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.couchbase.client.java.cluster.DefaultBucketSettings.builder;
import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.couchbase.builder.CouchbaseProvisioner.couchbase;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;


/**
 * A factory for a Couchbase backed {@link Example}.
 */
public class CouchbaseExample implements Example<CouchbaseAnnotatedMessage, CouchbaseAnnotatedMessage> {

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private String serverIpAddress;

    public CouchbaseExample(String serverIpAddress) {
        this.serverIpAddress = serverIpAddress;
    }

    @Override
    public Mache<String, CouchbaseAnnotatedMessage> exampleCache() throws Exception {

        final String keySpace = "NoSQL_MacheClient_Test_" + dateFormat.format(new Date());

        return mache(String.class, CouchbaseAnnotatedMessage.class)
                .cachedBy(guava())
                .storedIn(couchbase()
                        .withBucketSettings(builder().name(keySpace).quota(150).build())
                    .withNodes(serverIpAddress)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build())
                .withNoMessaging()
                .macheUp();
    }

    @Override
    public CouchbaseAnnotatedMessage createEntity(String primaryKey, String msg) {
        return new CouchbaseAnnotatedMessage(primaryKey, msg);
    }
}
