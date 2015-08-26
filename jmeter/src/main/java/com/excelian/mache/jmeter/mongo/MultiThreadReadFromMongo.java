package com.excelian.mache.jmeter.mongo;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;

import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.mongodb.ServerAddress;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiThreadReadFromMongo extends MacheAbstractJavaSamplerClient {
	private static final long serialVersionUID = 1193347578889214521L;
	
	private MongoDBCacheLoader<String, MongoTestEntity> db;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("ReadFromCassandra.setupTest");

        Map<String, String> mapParams = extractParameters(context);

        try {
            db = new MongoDBCacheLoader<>(MongoTestEntity.class, new CopyOnWriteArrayList<>(
                    Arrays.asList(new ServerAddress(mapParams
                            .get("mongo.server.ip.address"), 27017))),
            SchemaOptions.CREATEANDDROPSCHEMA, mapParams.get("keyspace.name"));
            db.create();//ensure we are connected and schema exists
        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (db != null) db.close();
    }
    
    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        Map<String, String> mapParams = extractParameters(context);
        int sleepMilis = Integer.parseInt(mapParams.get("read.sleepMs"));
        int timeoutMs = Integer.parseInt(mapParams.get("read.timeoutMs"));

        final SampleResult result = new SampleResult();
        result.sampleStart();

        final long startTime = new Date().getTime();
        MongoTestEntity readEntity = null;
        MongoTestEntity e = initMongoEntity(mapParams);

        do {
            try {
				readEntity = db.load(e.pkString);

	            if (readEntity == null) {
	                throw new Exception("No data found in db for key value of " + e.pkString);
	            }

	            try {
	                Thread.sleep(sleepMilis);
	            } catch (InterruptedException e1) {
	                getLogger().error("Error while reading value through cache from mongo " + e1.getMessage(), e1);
	                return super.setupResultForError(result, e1);
	            }
			} catch (Exception ex) {
	            setupResultForError(result, ex);
	            return result;
			}
        }
        while (new Date().getTime() - startTime <= timeoutMs && (readEntity == null || !e.description.equals(readEntity.description)));

        result.sampleEnd();
        result.setSuccessful(true);
        result.setResponseMessage("Put value (" + e.description + ") from Cache");

        return result;
    }

	protected MongoTestEntity initMongoEntity(Map<String, String> mapParams) {
		return new MongoTestEntity(mapParams.get("entity.key"),
				JMeterUtils.getProperty(mapParams.get("entity.value.name")));
	}
	
    @Override
    public Arguments getDefaultParameters() {
        final Arguments result = super.getDefaultParameters();
        result.addArgument("read.timeoutMs", "100");
        result.addArgument("read.sleepMs", "0");
        result.addArgument("entity.value.name", "mongo.entity.value");
        return result;
    }
}
