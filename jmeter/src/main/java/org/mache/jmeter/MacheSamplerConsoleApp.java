package org.mache.jmeter;

import com.google.common.cache.CacheLoader;
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
import java.util.concurrent.*;


public class MacheSamplerConsoleApp
{
    ActiveMQFactory mqFactory1 = null;
    ActiveMQFactory mqFactory2 = null;
    ExCache<String, TestEntity> cache1;
    ExCache<String, TestEntity> cache2;
    private Map<String, String> mapParams = getDefaultParameters();

    volatile boolean failTest = false;

    public static void main(String[] args) {
        new MacheSamplerConsoleApp().start();
    }

    public int start() {
        setupTest();
        runTest();
        teardownTest();
        return 0;
    }

    public void setupTest() {
        System.out.println("mache setupTest started  \n");

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
            System.out.println("Creating cache 1.");
            cache1 = cacheFactory1.createCache(new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));

            CacheFactoryImpl cacheFactory2 = new CacheFactoryImpl(mqFactory2, mqConfiguration, new CacheThingFactory(), new UUIDUtils());
            System.out.println("Creating cache 2.");
            cache2 = cacheFactory2.createCache(new MongoDBCacheLoader<String, TestEntity>(TestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));
            System.out.println("mache setupTest completed. "/* + cache1.getCacheLoader().getDriverSession().toString() */);

        } catch (Exception e) {
            System.out.println("mache error building factory");
            e.printStackTrace();
        }
    }

    public void teardownTest()
    {
        if(cache1!=null) cache1.close();
        if(cache2!=null) cache2.close();
        if(mqFactory1!=null) mqFactory1.close();
        if(mqFactory2!=null) mqFactory2.close();
    }

    public SampleResult runTest() {
        final SampleResult result = new SampleResult();
        failTest = false;

        result.sampleStart();
        System.out.println("Starting tests.");

        final ExecutorService executorService = Executors.newFixedThreadPool(1);

        final String pkString = "X1";
        final long thread1ReadTimeoutMilis = Long.parseLong(mapParams.get("thread1.read.timeoutMs"));
        final int thread1Iterations = Integer.parseInt(mapParams.get("thread1.iterations"));

        final Future<?> task1 = executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < thread1Iterations; ++i) {
                    final String expectedValue = String.valueOf(i);
                    final TestEntity e = new TestEntity(pkString, expectedValue);
                    System.out.println("TH " + Thread.currentThread() + " TH1 Putting value" + expectedValue);
                            System.out.println("TH1 Putting value " + expectedValue);
                    cache1.put(e.pkString, e);

                    final Date start = new Date();
                    TestEntity result = null;
                    do {
                        try {
                            try {
                                Thread.sleep(15);
                            } catch (InterruptedException e1) {
                                throw new RuntimeException(e1);
                            }

                            System.out.println("TH " + Thread.currentThread() + " TH1 Reading value");
                            result = cache2.get(e.pkString);
                            //TODO sometimes I get null there - don't fully udnerstand why
                            //TODO very rately I get correct value here
                            System.out.println("TH " + Thread.currentThread() + " TH1 Read value " + result.getDescription());
                        } catch (CacheLoader.InvalidCacheLoadException ex) {
                            if (!ex.getMessage().contains("CacheLoader returned null")) {
                                throw ex;
                            }

                            //ignore loading null
                        }
                    } while ((new Date().getTime()-start.getTime() <= thread1ReadTimeoutMilis) && (result == null || !expectedValue.equals(result.getDescription())));

                    System.out.println("TH1 Got correct value.");
                }
            }
        });

        try {
            System.out.println("Waiting for tasks to complete.");

            try {
                task1.get(500000L + thread1ReadTimeoutMilis * thread1Iterations, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("Could not complete task 1: ");
                e.printStackTrace();
            }

            System.out.println("Shutting down executor.");
            executorService.shutdown();
            executorService.awaitTermination(1000L, TimeUnit.MILLISECONDS);
            System.out.println("Shut down.");
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Could not complete all tasks: ");
            e.printStackTrace();
        }

        System.out.println("Shut down forcefully.");
        executorService.shutdownNow();
        System.out.println("Shut down.");

        if (!failTest) {
            result.sampleEnd();
            result.setSuccessful(true);
        }

        return result;
    }

    public Map<String, String> getDefaultParameters() {
        Map<String, String> defaultParameters = new HashMap<>();
        defaultParameters.put("mongo.server.ip.address", "10.28.1.140");
        defaultParameters.put("activemq.connection", "vm://localhost");
        defaultParameters.put("thread1.read.timeoutMs", "75");
        defaultParameters.put("thread1.iterations", "3");
        defaultParameters.put("thread2.timeoutMs", "20");
        defaultParameters.put("thread2.iterations", "0");
        return defaultParameters;
    }

}
