package com.excelian.mache.jmeter.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.excelian.mache.cassandra.CassandraCacheLoader;
import com.excelian.mache.cassandra.CassandraConfig;
import com.excelian.mache.cassandra.DefaultCassandraConfig;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.ActiveMQFactory;
import com.excelian.mache.events.integration.DefaultActiveMqConfig;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import com.excelian.mache.observable.MessageQueueObservableCacheFactory;
import com.excelian.mache.observable.utils.UUIDUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.IOException;
import java.util.Map;

public class CacheBackedByCassandra extends MacheAbstractJavaSamplerClient {
    MQFactory<String> mqFactory;
    Mache<String, CassandraTestEntity> cache;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("CacheBackedByCassandra.setupTest");

        Map<String, String> mapParams = extractParameters(context);
        String keySpace = mapParams.get("keyspace.name");

        MQConfiguration mqConfiguration = () -> "testTopic";

        try {
            mqFactory = new ActiveMQFactory<>(mapParams.get("activemq.connection"), new DefaultActiveMqConfig());

            final CassandraConfig config = new DefaultCassandraConfig();
            Cluster cluster = CassandraCacheLoader.connect(mapParams.get("server.ip.address"), mapParams.get("cluster.name"), 9042, config);
            CassandraCacheLoader<String, CassandraTestEntity> db = new CassandraCacheLoader<>(CassandraTestEntity.class, cluster, SchemaOptions.CREATESCHEMAIFNEEDED, keySpace, config);
            db.create();//this is to force the connection to occur within our setup

            MessageQueueObservableCacheFactory<String, CassandraTestEntity, Session> cacheFactory = new MessageQueueObservableCacheFactory<>(mqFactory, mqConfiguration, new MacheFactory(), new UUIDUtils());
            cache = cacheFactory.createCache(db);

            CassandraTestEntity entity = new CassandraTestEntity("dummy", "warmup");
            cache.put(entity.pkString, entity);
        } catch (Exception e) {
            getLogger().error("Error connecting to cassandra", e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (cache != null) {
            cache.close();
        }
        if (mqFactory != null) {
            try {
                mqFactory.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SampleResult runTest(JavaSamplerContext context) {

        Map<String, String> mapParams = extractParameters(context);
        SampleResult result = new SampleResult();
        boolean success = false;

        result.sampleStart();

        try {

            if (mapParams.get("action").contentEquals("read")) {
                String entityKey = mapParams.get("entity.key");
                CassandraTestEntity entity = cache.get(entityKey);

                if (entity == null) {
                    throw new Exception("No data found in cache for key value of " + entityKey);
                }

                result.setResponseMessage("Read " + entity.pkString + " from Cache");
            } else {
                String entityKey = mapParams.get("entity.key");
                String entityValue = mapParams.get("entity.value");

                CassandraTestEntity entity = new CassandraTestEntity(entityKey, entityValue);
                cache.put(entity.pkString, entity);

                result.setResponseMessage("Put " + entity.pkString + " into Cache");
            }
            success = true;
        } catch (Exception e) {
            setupResultForError(result, e);
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
        defaultParameters.addArgument("entity.key", "K${loopCounter}");
        defaultParameters.addArgument("entity.value", "Description for K${loopCounter}");
        defaultParameters.addArgument("action", "read");

        return defaultParameters;
    }
}