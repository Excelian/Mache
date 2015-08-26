package com.excelian.mache.jmeter.mongo;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.cassandra.DefaultCassandraConfig;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import com.excelian.mache.mongo.MongoDBCacheLoader;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.mongodb.ServerAddress;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReadFromMongo extends MacheAbstractJavaSamplerClient {

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
        SampleResult result = new SampleResult();
        boolean success = false;

        result.sampleStart();

        try {
            String keyValue = mapParams.get("entity.key");
            MongoTestEntity entity = db.load(keyValue);

            if (entity == null) {
                throw new Exception("No data found in db for key value of " + keyValue);
            }

            result.setResponseMessage("Read " + entity.pkString + " from database");
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