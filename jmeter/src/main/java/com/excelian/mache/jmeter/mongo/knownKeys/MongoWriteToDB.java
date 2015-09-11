package com.excelian.mache.jmeter.mongo.knownKeys;

import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.mongo.MacheAbstractMongoSamplerClient;
import com.excelian.mache.jmeter.mongo.MongoTestEntity;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.mongodb.ServerAddress;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by jbowkett on 11/09/2015.
 */
public class MongoWriteToDB extends MacheAbstractMongoSamplerClient {


    private static final long serialVersionUID = 3550175542777320608L;
    private MongoDBCacheLoader<String, MongoTestEntity> db;
    private ShuffledArray shuffledArray = new ShuffledArray();

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("ReadFromMongoDB.setupTest");

        final Map<String, String> mapParams = extractParameters(context);

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
        if (db != null) {
            db.close();
        }
    }


    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        result.sampleStart();
        writeOneThousandDocumentsToDbWithNewData();
        result.sampleEnd();
        result.setSuccessful(true);
        return result;
    }

    private void writeOneThousandDocumentsToDbWithNewData() {
        for (int i : shuffledArray.getShuffledListUpTo(1000)) {
            final String key = "document_" + i;
            final String value = key + "_" + System.currentTimeMillis();
            db.put(key, new MongoTestEntity(key, value));
        }
    }
}

