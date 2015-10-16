package com.excelian.mache.jmeter.mongo.knownKeys;

import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.mongo.AbstractMongoSamplerClient;
import com.excelian.mache.jmeter.mongo.MongoTestEntity;
import com.mongodb.ServerAddress;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;
import java.util.Map;

public class WriteToDB extends AbstractMongoSamplerClient {
    private static final long serialVersionUID = 4662847886347883622L;
    private AbstractCacheLoader<String, MongoTestEntity, ?> db;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info(getClass().getName() + ".setupTest");

        final Map<String, String> mapParams = extractParameters(context);

        try {
            db = mongodb()
                .withSeeds(new ServerAddress(mapParams.get("mongo.server.ip.address"), 27017))
                .withDatabase(mapParams.get("keyspace.name"))
                .withSchemaOptions(SchemaOptions.CREATE_SCHEMA_IF_NEEDED)
                .build().getCacheLoader(String.class, MongoTestEntity.class);
            db.create();// ensure we are connected and schema exists

        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (db != null) {
            db.close();
        }
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        result.sampleStart();
        try {
            writeDocumentToDbWithNewData(extractParameters(context));
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

    private void writeDocumentToDbWithNewData(final Map<String, String> params) {

        final String docNumber = params.get("entity.keyNo");
        final String entityValue = params.get("entity.value");
        final String key = "document_" + docNumber;
        final String value = (entityValue.equals("CURRENTTIME")) ? key + "_" + System.currentTimeMillis() : entityValue;

        getLogger().info("Writing to db key=" + key);
        db.put(key, new MongoTestEntity(key, value));
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();

        defaultParameters.addArgument("entity.value", "CURRENTTIME");
        return defaultParameters;
    }
}
