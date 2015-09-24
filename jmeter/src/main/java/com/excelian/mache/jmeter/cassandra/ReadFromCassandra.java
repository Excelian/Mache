package com.excelian.mache.jmeter.cassandra;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.util.Map;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;

public class ReadFromCassandra extends MacheAbstractJavaSamplerClient {

    private Mache<String, CassandraTestEntity> mache;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("ReadFromCassandra.setupTest");

        Map<String, String> mapParams = extractParameters(context);
        String keySpace = mapParams.get("keyspace.name");

        try {
            mache = mache(String.class, CassandraTestEntity.class)
                    .backedBy(cassandra()
                        .withCluster(Cluster.builder()
                            .addContactPoint(mapParams.get("server.ip.address"))
                            .withClusterName(mapParams.get("cluster.name"))
                            .withPort(9042)
                            .build())
                        .withKeyspace(keySpace)
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build())
                    .withNoMessaging()
                    .macheUp();

            mache.getCacheLoader().create();//ensure we are connected and schema exists
        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (mache != null) {
            mache.close();
        }
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        Map<String, String> mapParams = extractParameters(context);
        SampleResult result = new SampleResult();
        result.sampleStart();

        try {
            String keyValue = mapParams.get("entity.key");
            CassandraTestEntity entity = mache.get(keyValue);

            if (entity == null) {
                throw new Exception("No data found in db for key value of " + keyValue);
            }

            result.setResponseMessage("Read " + entity.pkString + " from database");
        } catch (Exception e) {
            setupResultForError(result, e);
            return result;
        }

        result.sampleEnd();
        result.setSuccessful(true);
        return result;
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("keyspace.name", "JMeterReadThrough");
        defaultParameters.addArgument("server.ip.address", "10.28.1.140");
        defaultParameters.addArgument("cluster.name", "BluePrint");
        defaultParameters.addArgument("entity.key", "K1");
        return defaultParameters;
    }

}