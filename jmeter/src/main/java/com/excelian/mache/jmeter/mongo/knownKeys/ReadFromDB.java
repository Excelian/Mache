package com.excelian.mache.jmeter.mongo.knownKeys;

import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.mongo.AbstractMongoSamplerClient;
import com.excelian.mache.jmeter.mongo.MongoTestEntity;
import com.mongodb.ServerAddress;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongoConnectionContext;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;

import java.util.List;
import java.util.Map;

public class ReadFromDB extends AbstractMongoSamplerClient {
    private static final long serialVersionUID = 251140199032740124L;

    protected ConnectionContext<List<ServerAddress>> connectionContext;
    private MacheLoader db;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("ReadFromDB.setupTest");

        final Map<String, String> mapParams = extractParameters(context);

        try {
            connectionContext=mongoConnectionContext(new ServerAddress(mapParams.get("mongo.server.ip.address"), 27017));

            final Mache<String, MongoTestEntity> mache = mache(String.class, MongoTestEntity.class)
                .backedBy(mongodb()
                    .withContext(connectionContext)
                    .withDatabase(mapParams.get("keyspace.name"))
                    .withSchemaOptions(SchemaOptions.CREATE_SCHEMA_IF_NEEDED)
                    .build()).withNoMessaging().macheUp();
            db = mache.getCacheLoader();
            db.create();

        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (db != null) {
            db.close();
        }
        if(connectionContext!=null) {
            try {
                connectionContext.close();
            } catch (Exception e) {
                getLogger().error("mache disconnection from mongo", e);
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
