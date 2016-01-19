package com.excelian.mache.jmeter.couch.knownKeys;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.couch.AbstractCouchSamplerClient;
import com.excelian.mache.jmeter.couch.CouchTestEntity;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.couchbase.builder.CouchbaseProvisioner.couchbase;
import static com.couchbase.client.java.cluster.DefaultBucketSettings.builder;
import static com.excelian.mache.couchbase.builder.CouchbaseProvisioner.couchbaseConnectionContext;


import java.util.Map;

public class ReadFromDB extends AbstractCouchSamplerClient {
    private static final long serialVersionUID = 251140199032740124L;
    private MacheLoader db;
    private ConnectionContext<Cluster> connectionContext;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("ReadFromDB.setupTest");

        final Map<String, String> mapParams = extractParameters(context);

        try {
            final String keySpace = mapParams.get("keyspace.name");
            final String couchServer = mapParams.get("couch.server.ip.address");

            connectionContext = couchbaseConnectionContext(couchServer, DefaultCouchbaseEnvironment.create());

            final Mache<String, CouchTestEntity> mache = mache(String.class, CouchTestEntity.class)
                .backedBy(couchbase().withContext(connectionContext)
                        .withBucketSettings(builder().name(keySpace).quota(150).build())
                        .withSchemaOptions(SchemaOptions.CREATE_SCHEMA_IF_NEEDED)
                        .build())
                .withNoMessaging()
                .macheUp();

            db = mache.getCacheLoader();
            db.create();

        } catch (Exception e) {
            getLogger().error("Error connecting to couchbase", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (db != null) {
            db.close();
        }

        if (connectionContext != null) {
            try {
                connectionContext.close();
            } catch (Exception e) {
                getLogger().error("Error disconnecting from couchbase", e);
            }
        }
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        result.sampleStart();
        try {
            readDocumentFromDb(extractParameters(context));
            result.sampleEnd();
            result.setSuccessful(true);
        } catch (Exception e) {
            result.sampleEnd();
            result.setSuccessful(false);
            getLogger().error("Error running test", e);
            result.setResponseMessage(e.toString());
        }
        return result;
    }

    private void readDocumentFromDb(final Map<String, String> params) throws Exception {
        final String docNumber = params.get("entity.keyNo");
        final String key = "document_" + docNumber;
        db.load(key);
    }
}
