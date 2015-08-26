package com.excelian.mache.jmeter.mongo;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.fasterxml.uuid.Generators;
import com.mongodb.ServerAddress;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiThreadWriteToMongo extends
MacheAbstractJavaSamplerClient {
	private static final long serialVersionUID = 2529296764873734718L;
	
	private MongoDBCacheLoader<String, MongoTestEntity> db;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("MultiThreadWriteToMongo.setupTest");
        Map<String, String> mapParams = extractParameters(context);

        try {
            db = new MongoDBCacheLoader<>(MongoTestEntity.class, new CopyOnWriteArrayList<>(
                    Arrays.asList(new ServerAddress(mapParams
                            .get("mongo.server.ip.address"), 27017))),
            SchemaOptions.CREATEANDDROPSCHEMA, mapParams.get("keyspace.name"));
            db.create();
        } catch (Exception e) {
            getLogger().error("Error connecting to mongo", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (db != null) db.close();
    }
    
	@Override
	public SampleResult runTest(JavaSamplerContext context) {
		Map<String, String> mapParams = extractParameters(context);
		final SampleResult result = new SampleResult();

		result.sampleStart();

		try {

			MongoTestEntity e = initMongoEntity(mapParams);
			db.put(e.pkString, e);
			JMeterUtils.setProperty(mapParams.get("entity.value.name"), e.description);

			result.sampleEnd();
			result.setSuccessful(true);
			result.setResponseMessage("Put value (" + e.description
					+ ") from Cache");
		} catch (Exception e1) {
			return super.setupResultForError(result, e1);
		}

		return result;
	}

	protected MongoTestEntity initMongoEntity(Map<String, String> mapParams) {
		return new MongoTestEntity(mapParams.get("entity.key"),
				Generators.timeBasedGenerator().generate().toString());
	}

	@Override
	public Arguments getDefaultParameters() {
		Arguments defaultParameters = super.getDefaultParameters();
        defaultParameters.addArgument("mongo.server.ip.address", "10.28.1.140");
        defaultParameters.addArgument("activemq.connection", "vm://localhost");
        defaultParameters.addArgument("entity.key", "K${loopCounter}");
		defaultParameters.addArgument("entity.value.name", "mongo.entity.value");
        defaultParameters.addArgument("keyspace.name", "JMeterNoCache");
		return defaultParameters;
	}
}
