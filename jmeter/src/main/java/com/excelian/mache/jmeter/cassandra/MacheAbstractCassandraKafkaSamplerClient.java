package com.excelian.mache.jmeter.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.events.integration.KafkaMqConfig;
import com.excelian.mache.events.integration.builder.KafkaMessagingProvisioner;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.util.Map;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandraConnectionContext;

@SuppressWarnings("serial")
public abstract class MacheAbstractCassandraKafkaSamplerClient extends AbstractCassandraSamplerClient {

    protected Mache<String, CassandraTestEntity> cache1 = null;
    protected ConnectionContext<Cluster> connectionContext;

    @Override
    public void setupTest(JavaSamplerContext context) {
        Map<String, String> mapParams = extractParameters(context);
        getLogger().info("mache setupTest started  \n");

        try {
            createCache(mapParams);
            getLogger().info("mache setupTest completed. ");

            getLogger().info("cache connection completed cache is " + cache1);
        } catch (Exception e) {
            getLogger().error("mache error building factory", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void teardownTest(JavaSamplerContext context) {
        if (cache1 != null) {
            cache1.close();
        }
        if (connectionContext != null) {
            try {
                connectionContext.close();
            } catch (Exception e) {
                getLogger().error("mache error closing db session", e);
            }
        }
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();

        defaultParameters.addArgument("kafka.connection", "192.168.3.4");
        defaultParameters.addArgument("kafka.topic", "Kafka_JmeterCassandraTest");
        return defaultParameters;
    }

    protected void createCache(Map<String, String> mapParams) throws Exception {
        final KafkaMessagingProvisioner kafkaProvisioner = KafkaMessagingProvisioner.kafka()
                .withKafkaMqConfig(KafkaMqConfig.KafkaMqConfigBuilder.builder()
                        .withZkHost(mapParams.get("kafka.connection")).build())
                .withTopic(mapParams.get("kafka.topic"));

        connectionContext = cassandraConnectionContext(Cluster.builder().withClusterName("BluePrint")
                .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM))
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
                .addContactPoint(mapParams.get("cassandra.server.ip.address")).withPort(9042));

        cache1 = mache(String.class, CassandraTestEntity.class).backedBy(cassandra()
                .withConnectionContext(connectionContext)
                .withKeyspace(mapParams.get("keyspace.name")).withSchemaOptions(SchemaOptions.CREATE_SCHEMA_IF_NEEDED)
                .build()).withMessaging(kafkaProvisioner).macheUp();
    }
}