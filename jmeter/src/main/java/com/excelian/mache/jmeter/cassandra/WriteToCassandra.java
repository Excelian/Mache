package com.excelian.mache.jmeter.cassandra;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.CassandraCacheLoader;
import com.excelian.mache.SchemaOptions;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Map;

public class WriteToCassandra extends MacheAbstractJavaSamplerClient {

    private CassandraCacheLoader<String, CassandraTestEntity> db;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("WriteToCassandra.setupTest");
        Map<String, String> mapParams = ExtractParameters(context);
        String keySpace = mapParams.get("keyspace.name");

        try {
            Cluster cluster = CassandraCacheLoader.connect(
                    mapParams.get("server.ip.address"), mapParams.get("cluster.name")
                    , 9042);
            db = new CassandraCacheLoader<>(CassandraTestEntity.class, cluster, SchemaOptions.CREATESCHEMAIFNEEDED, keySpace);

            db.create(db.getName(), "");
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

        Map<String, String> mapParams = ExtractParameters(context);
        SampleResult result = new SampleResult();
        boolean success = false;

        result.sampleStart();

        try {
            CassandraTestEntity t1 = new CassandraTestEntity(mapParams.get("entity.key"), mapParams.get("entity.value"));
            db.put(t1.pkString, t1);
            result.setResponseMessage("Created " + t1.pkString);
            success = true;
        } catch (Exception e) {

            SetupResultForError(result, e);
            return result;
        }

        result.sampleEnd();
        result.setSuccessful(success);
        return result;
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("keyspace.name", "JMeterReadThrough");
        defaultParameters.addArgument("server.ip.address", "10.28.1.140");
        defaultParameters.addArgument("cluster.name", "BluePrint");
        defaultParameters.addArgument("entity.key", "K1");
        defaultParameters.addArgument("entity.value", "ValueOne");
        return defaultParameters;
    }

}