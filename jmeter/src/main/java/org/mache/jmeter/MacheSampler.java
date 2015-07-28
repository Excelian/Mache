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
import org.mache.jmeter.mongo.MongoTestEntity;
import org.mache.utils.UUIDUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;


public class MacheSampler extends AbstractJavaSamplerClient implements Serializable
{
    ActiveMQFactory mqFactory1 = null;
    ActiveMQFactory mqFactory2 = null;
    ExCache<String, MongoTestEntity> cache1;
    ExCache<String, MongoTestEntity> cache2;
    private Map<String, String> mapParams = new HashMap<String, String>();

    volatile boolean failTest = false;

    @Override
    public void setupTest(JavaSamplerContext context) {

        getLogger().info("mache setupTest started  \n");
        mapParams=ExtractParameters(context);

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
            cache1 = cacheFactory1.createCache(new MongoDBCacheLoader<String, MongoTestEntity>(MongoTestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));

            CacheFactoryImpl cacheFactory2 = new CacheFactoryImpl(mqFactory2, mqConfiguration, new CacheThingFactory(), new UUIDUtils());
            getLogger().info("Creating cache 2.");
            cache2 = cacheFactory2.createCache(new MongoDBCacheLoader<String, MongoTestEntity>(MongoTestEntity.class, serverAddresses, SchemaOptions.CREATEANDDROPSCHEMA, keySpace));
            getLogger().info("mache setupTest completed. "/* + cache1.getCacheLoader().getDriverSession().toString() */);

        } catch (Exception e) {
            getLogger().error("mache error building factory", e);
        }
    }

    static private Map<String, String> ExtractParameters(JavaSamplerContext context) {
        Map<String, String> mapParams = new ConcurrentHashMap<>();
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
        if(cache1!=null) cache1.close();
        if(cache2!=null) cache2.close();
        if(mqFactory1!=null) mqFactory1.close();
        if(mqFactory2!=null) mqFactory2.close();
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        final SampleResult result = new SampleResult();
        failTest = false;

        result.sampleStart();
        getLogger().info("Starting tests.");

        final ExecutorService executorService = Executors.newFixedThreadPool(3);

        final String pkString = "X1";
        final long thread1ReadTimeoutMilis = Long.parseLong(mapParams.get("thread1.read.timeoutMs"));
        final int thread1Iterations = Integer.parseInt(mapParams.get("thread1.iterations"));

        final Future<?> task1 = executorService.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < thread1Iterations; ++i) {
                    final String expectedValue = String.valueOf(i);
                    final MongoTestEntity e = new MongoTestEntity(pkString, expectedValue);
                    getLogger().info("TH " + Thread.currentThread() + " TH1 Putting value" + expectedValue);
                            System.out.println("TH1 Putting value " + expectedValue);
                    cache1.put(e.pkString, e);

                    final Future<MongoTestEntity> readTask = executorService.submit(new Callable<MongoTestEntity>() {
                        @Override
                        public MongoTestEntity call() throws Exception {
                            MongoTestEntity result = null;
                            do {
                                try {
                                    Thread.sleep(15);
                                    System.out.println("TH " + Thread.currentThread() + " TH1 Reading value");
                                    result = cache2.get(e.pkString);
                                    getLogger().info("TH " + Thread.currentThread() + " TH1 Read value " + result.description);
                                } catch (CacheLoader.InvalidCacheLoadException e) {
                                    if (!e.getMessage().contains("CacheLoader returned null")) {
                                        throw e;
                                    }

                                    //ignore loading null
                                }
                            } while (result == null || !expectedValue.equals(result.description));

                            getLogger().info("TH1 Got correct value.");
                            return result;
                        }
                    });

                    try {
                        readTask.get(thread1ReadTimeoutMilis, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException | ExecutionException eex) {
                        getLogger().error("Error while waiting for read task in thread 1", eex);
                        result.sampleEnd();
                        result.setSuccessful(false);
                        result.setResponseMessage("Exception: " + eex);
                        failTest = true;
                    } catch (TimeoutException tex) {
                        //ignore timeout
                    } finally {
                        readTask.cancel(true);
                    }
                }
            }
        });

        final long thread2TimeoutMilis = Long.parseLong(mapParams.get("thread2.timeoutMs"));
        final int thread2Iterations = Integer.parseInt(mapParams.get("thread2.iterations"));
        final Future<?> task2 = executorService.submit(new Runnable() {
            @Override
            public void run() {
                final Random r = new Random();
                for (int i = 0; i < thread2Iterations; ++i) {
                    final String cache1Value = String.valueOf(r.nextInt());
                    final String cache2Value = String.valueOf(r.nextInt());

                    getLogger().info("TH2 putting new random values " + cache1Value + ", " + cache2Value);

                    cache1.put(pkString, new MongoTestEntity(pkString, cache1Value));
                    cache2.put(pkString, new MongoTestEntity(pkString, cache2Value));

                    try {
                        Thread.sleep(thread2TimeoutMilis);
                    } catch (InterruptedException e) {
                        getLogger().error("Error occured in thread2.", e);
                        result.sampleEnd();
                        result.setSuccessful(false);
                        result.setResponseMessage("Exception " + e);
                        failTest = true;
                    }
                }
            }
        });

        try {
            getLogger().info("Waiting for tasks to complete.");

            try {
                task1.get(5000L + thread1ReadTimeoutMilis * thread1Iterations, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                getLogger().error("Could not complete task 1: ", e);
            }

            try {
                task1.get(5000L + thread2TimeoutMilis * thread2Iterations, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                getLogger().error("Could not complete task 1: ", e);
            }

            getLogger().info("Shutting down executor.");
            executorService.shutdown();
            executorService.awaitTermination(10000L, TimeUnit.MILLISECONDS);
            getLogger().info("Shut down.");
        } catch (InterruptedException | ExecutionException e) {
            getLogger().error("Could not complete all tasks: ", e);
        }

        getLogger().info("Shut down forcefully.");
        executorService.shutdownNow();
        getLogger().info("Shut down.");

        if (!failTest) {
            result.sampleEnd();
            result.setSuccessful(true);
        }

        return result;
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("mongo.server.ip.address", "10.28.1.140");
        defaultParameters.addArgument("activemq.connection", "vm://localhost");
        defaultParameters.addArgument("thread1.read.timeoutMs", "100");
        defaultParameters.addArgument("thread1.iterations", "1000");
        defaultParameters.addArgument("thread2.timeoutMs", "20");
        defaultParameters.addArgument("thread2.iterations", "10000");
        return defaultParameters;
    }

}
