package com.excelian.mache.jmeter.cassandra;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.core.Mache;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.events.integration.builder.ActiveMQMessagingProvisioner;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.io.IOException;
import java.util.Map;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;

public class CacheBackedByCassandra extends MacheAbstractJavaSamplerClient {
    Mache<String, CassandraTestEntity> cache;

    @Override
    public void setupTest(JavaSamplerContext context) {
        getLogger().info("CacheBackedByCassandra.setupTest");

        Map<String, String> mapParams = extractParameters(context);
        String keySpace = mapParams.get("keyspace.name");

        MQConfiguration mqConfiguration = () -> "testTopic";

        try {
            cache = mache(String.class, CassandraTestEntity.class)
                    .backedBy(cassandra()
                            .withCluster(Cluster.builder()
                                    .withClusterName(mapParams.get("cluster.name"))
                                    .addContactPoint(mapParams.get("server.ip.address"))
                                    .withPort(9042)
                                    .build())
                            .withKeyspace(keySpace)
                            .build())
                    .withMessaging(ActiveMQMessagingProvisioner.activemq()
                            .withTopic("testTopic")
                            .withConnectionFactory(new ActiveMQConnectionFactory(mapParams.get("activemq.connection")))
                            .build())
                    .macheUp();

            cache.getCacheLoader().create();//this is to force the connection to occur within our setup

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