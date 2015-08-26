package com.excelian.mache.jmeter.mongo;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.mongodb.ServerAddress;

public class WriteToMongo extends MacheAbstractJavaSamplerClient {
	private static final long serialVersionUID = -2893568669204762293L;
	
	private MongoDBCacheLoader<String, MongoTestEntity> db;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("WriteToMongo.setupTest");
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
        SampleResult result = new SampleResult();
        boolean success = false;
        result.sampleStart();
        try {
        	MongoTestEntity t1 = new MongoTestEntity(mapParams.get("entity.key"), mapParams.get("entity.value"));
            db.put(t1.pkString, t1);
            result.setResponseMessage("Created " + t1.pkString);
            success = true;
        } catch (Exception e) {
            setupResultForError(result, e);
            return result;
        }
        result.sampleEnd();
        result.setSuccessful(success);
        return result;
    }
    
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("mongo.server.ip.address", "10.28.1.140");
        defaultParameters.addArgument("activemq.connection", "vm://localhost");
        defaultParameters.addArgument("entity.key", "K${loopCounter}");
        defaultParameters.addArgument("entity.value", "V${loopCounter}");
        defaultParameters.addArgument("keyspace.name", "JMeterNoCache");
        return defaultParameters;
    }
}