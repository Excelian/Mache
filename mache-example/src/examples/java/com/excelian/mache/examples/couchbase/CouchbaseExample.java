package com.excelian.mache.examples.couchbase;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.couchbase.client.java.cluster.DefaultBucketSettings.builder;
import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.couchbase.builder.CouchbaseProvisioner.couchbase;

/**
 * A factory for a Couchbase backed {@link Example}.
 */
public class CouchbaseExample implements Example<CouchbaseAnnotatedMessage> {

    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    @Override
    public Mache<String, CouchbaseAnnotatedMessage> exampleCache() throws Exception {

        Cluster cluster = CouchbaseCluster.create(DefaultCouchbaseEnvironment.create(), "10.28.1.140");

        final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
        return mache(String.class, CouchbaseAnnotatedMessage.class)
                .backedBy(couchbase()
                        .withCluster(cluster)
                        .withBucketSettings(builder().name(keySpace).quota(150).build())
                        .withDefaultAdminDetails()
                        .withDefaultSchemaOptions()
                        .build())
                .withNoMessaging()
                .macheUp();
    }
}
