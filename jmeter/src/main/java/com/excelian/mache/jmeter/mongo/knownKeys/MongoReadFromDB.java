package com.excelian.mache.jmeter.mongo.knownKeys;

import java.util.Map;

import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.mongo.MacheAbstractMongoSamplerClient;
import com.excelian.mache.jmeter.mongo.MongoTestEntity;
import com.excelian.mache.mongo.builder.MongoDBProvisioner;
import com.mongodb.ServerAddress;

public class MongoReadFromDB extends MacheAbstractMongoSamplerClient {

	private ShuffledSequence shuffledSequence = new ShuffledSequence();

	private static final long serialVersionUID = 3550175542777320608L;
	private AbstractCacheLoader<String, MongoTestEntity, ?> db;

	@Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("ReadFromMongoDB.setupTest");

        final Map<String, String> mapParams = extractParameters(context);

        try {
        	db = MongoDBProvisioner.mongodb().withSeeds(new ServerAddress(mapParams
                    .get("mongo.server.ip.address"), 27017))
        			.withDatabase(mapParams.get("keyspace.name"))
        			.withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
        			.build()
        			.getCacheLoader(String.class, MongoTestEntity.class);
            db.create();//ensure we are connected and schema exists
        } catch (Exception e) {
            getLogger().error("Error connecting to mongo", e);
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
			readOneThousandDocumentsFromDb();
			result.sampleEnd();
			result.setSuccessful(true);
		} catch (Exception e) {
			result.sampleEnd();
			result.setSuccessful(false);
			getLogger().error("Error connecting to mongo", e);
		}
		return result;
	}

	private void readOneThousandDocumentsFromDb() throws Exception {
		for (int i : shuffledSequence.upTo(1000)) {
			final String key = "document_" + i;
			db.load(key);
		}
	}
}
