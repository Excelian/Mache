package org.mache.jmeter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.mache.CacheFactoryImpl;
import org.mache.CacheThingFactory;
import org.mache.ExCache;
import org.mache.MongoDBCacheLoader;
import org.mache.SchemaOptions;
import org.mache.events.MQConfiguration;
import org.mache.events.integration.ActiveMQFactory;
import org.mache.utils.UUIDUtils;

import com.mongodb.ServerAddress;

public class MongoRandomLoad extends MacheAbstractJavaSamplerClient {
	private static final long serialVersionUID = -6670630256079770024L;
	
	ActiveMQFactory mqFactory1 = null;
    ActiveMQFactory mqFactory2 = null;
    ExCache<String, TestEntity> cache1;
    ExCache<String, TestEntity> cache2;
    final Random r = new Random();

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("mache setupTest started  \n");
        ExtractParameters(context);

        List<ServerAddress> serverAddresses = new CopyOnWriteArrayList<>(Arrays.asList(new ServerAddress(mapParams.get("mongo.server.ip.address"), 27017)));
        String keySpace = "JMeter_Test_" + new Date().toString();

        MQConfiguration mqConfiguration = new MQConfiguration() {
            @Override
            public String getTopicName() {
                return "testTopic";
            }
        };

        try {
            mqFactory1 = new ActiveMQFactory(mapParams.get("activemq.connection"));
            mqFactory2 = new ActiveMQFactory(mapParams.get("activemq.connection"));
            CacheFactoryImpl cacheFactory1 = new CacheFactoryImpl(mqFactory1, mqConfiguration, new CacheThingFactory(), new UUIDUtils());
            getLogger().info("Creating cache 1.");
            cache1 = cacheFactory1.createCache(new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));

            CacheFactoryImpl cacheFactory2 = new CacheFactoryImpl(mqFactory2, mqConfiguration, new CacheThingFactory(), new UUIDUtils());
            getLogger().info("Creating cache 2.");
            cache2 = cacheFactory2.createCache(new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));
            getLogger().info("mache setupTest completed. ");

        } catch (Exception e) {
            getLogger().error("mache error building factory", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context)
    {
        if(cache1!=null) cache1.close();
        if(cache2!=null) cache2.close();
        if(mqFactory1!=null) mqFactory1.close();
        if(mqFactory2!=null) mqFactory2.close();
    }

	@Override
	public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();

        result.sampleStart();
		
		final String pkString = mapParams.get("entity.key");
        final long timeoutMilis = Long.parseLong(mapParams.get("timeoutMs"));
        final String cache1Value = String.valueOf(r.nextInt());
        final String cache2Value = String.valueOf(r.nextInt());

        getLogger().debug("Putting new random values " + cache1Value + ", " + cache2Value);

        cache1.put(pkString, new TestEntity(pkString, cache1Value));
        cache2.put(pkString, new TestEntity(pkString, cache2Value));

        try {
            Thread.sleep(timeoutMilis);
            result.sampleEnd();
            result.setSuccessful(true);
            result.setResponseMessage("Written values (" + cache1Value + ", " + cache2Value + ") to Cache");
        } catch (InterruptedException e) {
        	SetupResultForError(result, e);
        }
        
        return result;
	}
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("mongo.server.ip.address", "10.28.1.140");
        defaultParameters.addArgument("activemq.connection", "vm://localhost");
        defaultParameters.addArgument("timeoutMs", "20");
        defaultParameters.addArgument("entity.key", "X1");
        return defaultParameters;
    }

}
