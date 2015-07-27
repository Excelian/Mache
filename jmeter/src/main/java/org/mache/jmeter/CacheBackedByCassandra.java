package org.mache.jmeter;

import com.datastax.driver.core.Cluster;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.mache.*;
import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;
import org.mache.events.integration.ActiveMQFactory;
import org.mache.utils.UUIDUtils;

import java.io.IOException;
import java.util.Map;

public class CacheBackedByCassandra extends MacheAbstractJavaSamplerClient
{
    MQFactory mqFactory;
    ExCache<String, CassandraTestEntity> cache;

    @Override
    public void setupTest(JavaSamplerContext context) {
        System.out.println("CacheBackedByCassandra.setupTest");

        Map<String, String> mapParams=ExtractParameters(context);
        String keySpace = mapParams.get("keyspace.name");

        MQConfiguration mqConfiguration = () -> "testTopic";

        try {
            mqFactory = new ActiveMQFactory(mapParams.get("activemq.connection"));

            Cluster cluster = CassandraCacheLoader.connect( mapParams.get("server.ip.address"), mapParams.get("cluster.name") , 9042);
            CassandraCacheLoader<String, CassandraTestEntity> db= new CassandraCacheLoader(CassandraTestEntity.class, cluster, SchemaOptions.CREATESCHEMAIFNEEDED, keySpace);

            CacheFactoryImpl cacheFactory = new CacheFactoryImpl(mqFactory, mqConfiguration, new CacheThingFactory(), new UUIDUtils());
            cache = cacheFactory.createCache(db);
        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context)
    {
        if(cache!=null) cache.close();
        if(mqFactory!=null) try {
            mqFactory.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        Map<String, String> mapParams=ExtractParameters(context);
        SampleResult result = new SampleResult();
        boolean success = false;

        result.sampleStart();

        try {
            String keyValue=mapParams.get("entity.key");
            CassandraTestEntity entity= cache.get(keyValue);

            if(entity==null)
            {
                throw new Exception("No data found in cache for key value of "+keyValue);
            }

            result.setResponseMessage("Read " + entity.pkString+ " from Cache");
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
        defaultParameters.addArgument("activemq.connection", "vm://localhost");
        defaultParameters.addArgument("entity.key", "K1");
        return defaultParameters;
    }
}