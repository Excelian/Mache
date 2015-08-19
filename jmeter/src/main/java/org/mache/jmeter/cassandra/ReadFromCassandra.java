package org.mache.jmeter.cassandra;

import com.datastax.driver.core.Cluster;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.mache.CassandraCacheLoader;
import org.mache.SchemaOptions;
import org.mache.jmeter.MacheAbstractJavaSamplerClient;
import org.mache.jmeter.cassandra.CassandraTestEntity;

import java.util.Map;

public class ReadFromCassandra extends MacheAbstractJavaSamplerClient
{

    private CassandraCacheLoader<String, CassandraTestEntity> db;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("ReadFromCassandra.setupTest");

        Map<String, String> mapParams=ExtractParameters(context);
        String keySpace = mapParams.get("keyspace.name");

        try {
            Cluster cluster = CassandraCacheLoader.connect(
                    mapParams.get("server.ip.address"), mapParams.get("cluster.name") , 9042);
            db= new CassandraCacheLoader(CassandraTestEntity.class, cluster, SchemaOptions.CREATESCHEMAIFNEEDED, keySpace);
            db.create("","");//ensure we are connected and schema exists
        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context)
    {
        if(db!=null) db.close();
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        Map<String, String> mapParams=ExtractParameters(context);
        SampleResult result = new SampleResult();
        boolean success = false;

        result.sampleStart();

        try {
            String keyValue=mapParams.get("entity.key");
            CassandraTestEntity entity= db.load(keyValue);

            if(entity==null)
            {
                throw new Exception("No data found in db for key value of "+keyValue);
            }

            result.setResponseMessage("Read " + entity.pkString+ " from database");
            success=true;
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
        return defaultParameters;
    }

}