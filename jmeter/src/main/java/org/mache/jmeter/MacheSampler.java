package org.mache.jmeter;

import com.mongodb.ServerAddress;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.mache.*;
import org.mache.events.MQConfiguration;
import org.mache.events.integration.ActiveMQFactory;
import org.mache.utils.UUIDUtils;

import java.io.Serializable;
import java.util.*;


public class MacheSampler extends AbstractJavaSamplerClient implements Serializable
{
    ActiveMQFactory mqFactory = null;
    ExCache<String, TestEntity> cache;
    private Map<String, String> mapParams = new HashMap<String, String>();

    @Override
    public void setupTest(JavaSamplerContext context) {

        getLogger().info("mache setupTest started  \n");
        mapParams=ExtractParameters(context);

        List<ServerAddress> serverAddresses = Arrays.asList(new ServerAddress(mapParams.get("mongo.server.ip.address"), 27017));
        String keySpace = "JMeter_Test_" + new Date().toString();

        MQConfiguration mqConfiguration = new MQConfiguration() {
            @Override
            public String getTopicName() {
                return "testTopic";
            }
        };

        try {
            mqFactory = new ActiveMQFactory(mapParams.get("activemq.connection"));
            CacheFactoryImpl cacheFactory = new CacheFactoryImpl(mqFactory, mqConfiguration, new CacheThingFactory(), new UUIDUtils());
            cache = cacheFactory.createCache(new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));
            getLogger().info("mache setupTest completed. " + cache.getCacheLoader().getDriverSession().toString() );

        } catch (Exception e) {
            getLogger().error("mache error building factory", e);
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
        if(cache!=null) cache.close();
        if(mqFactory!=null) mqFactory.close();
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        boolean success = false;

        result.sampleStart();

        // Write your test code here.
        try {
            TestEntity t1= new TestEntity("X1");
            cache.put(t1.pkString, t1);
            result.setResponseMessage("Mache test completed");
            success=true;
        } catch (Exception e) {
            getLogger().error("mache runTest", e);
            result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e);

            return result;
        }
        //
        result.sampleEnd();
        result.setSuccessful(success);
        return result;
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("mongo.server.ip.address", "10.28.1.140");
        defaultParameters.addArgument("activemq.connection", "vm://localhost");
        return defaultParameters;
    }

}
