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

public class MongoReadWrite extends MacheAbstractJavaSamplerClient {
	private static final long serialVersionUID = 3550175542777320608L;
	
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
		final long sleepMilis = Long.parseLong(mapParams.get("read.sleepMs"));
        final long timeoutMilis = Long.parseLong(mapParams.get("read.timeoutMs"));
        final int expectedValue = r.nextInt();
		final String cache1Value = String.valueOf(expectedValue);

        final TestEntity e = new TestEntity(pkString, cache1Value);
        cache1.put(e.pkString, e);

        getLogger().info("Waiting to receive value " + cache1Value + "...");
        
        Date start = new Date();
        boolean success = false;
        do {
        	if (cache1Value.equals(cache2.get(e.pkString).getDescription())) {
        		success = true;
        		getLogger().info("Got correct value.");
        		break;
        	}
        	
            try {
            	Thread.sleep(sleepMilis);
            } catch (InterruptedException ex) {
            	SetupResultForError(result, ex);
            	return result;
            }
        } while (new Date().getTime() - start.getTime() <= timeoutMilis);

        result.sampleEnd();
        result.setSuccessful(success);
        
        if (success) {
        	result.setResponseMessage("Got correct value (" + cache1Value + ") from Cache");
        } else {
        	result.setResponseMessage("Timeout while trying to get correct value (" + cache1Value + ") from Cache");
        }

        return result;
	}
	
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("mongo.server.ip.address", "10.28.1.140");
        defaultParameters.addArgument("activemq.connection", "vm://localhost");
        defaultParameters.addArgument("read.sleepMs", "2");
        defaultParameters.addArgument("read.timeoutMs", "50");
        defaultParameters.addArgument("entity.key", "X1");
        return defaultParameters;
    }

}
