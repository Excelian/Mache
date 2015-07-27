package org.mache.jmeter;

import com.datastax.driver.core.Cluster;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.mache.CassandraCacheLoader;
import org.mache.SchemaOptions;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReadFromCassandra extends AbstractJavaSamplerClient implements Serializable
{

    private CassandraCacheLoader<String, CassandraTestEntity> db;

    @Override
    public void setupTest(JavaSamplerContext context) {

        Map<String, String> mapParams=ExtractParameters(context);
        String keySpace = mapParams.get("keyspace.name");

        try {
            Cluster cluster = CassandraCacheLoader.connect(
                    mapParams.get("server.ip.address"), mapParams.get("cluster.name") , 9042);
            db= new CassandraCacheLoader(CassandraTestEntity.class, cluster, SchemaOptions.CREATESCHEMAIFNEEDED, keySpace);

            db.create("","");
        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    static private Map<String, String> ExtractParameters(JavaSamplerContext context) {
        Map<String, String> mapParams = new HashMap<String, String>();
        for (Iterator<String> it = context.getParameterNamesIterator(); it.hasNext();) {
            String paramName =  it.next();
            String paramValue = context.getParameter(paramName);
            mapParams.put(paramName, paramValue);
        }
        return mapParams;
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

            result.setResponseMessage("Read " + entity.pkString+ " from db");
            success=true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);

            result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e);

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