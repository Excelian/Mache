package com.excelian.mache.jmeter.cassandra;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.guava.builder.GuavaProvisioner.guava;

import java.util.Map;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.events.integration.KafkaMqConfig;
import com.excelian.mache.events.integration.builder.KafkaMessagingProvisioner;

@SuppressWarnings("serial")
public abstract class MacheAbstractCassandraKafkaSamplerClient extends AbstractCassandraSamplerClient {

	protected Mache<String, CassandraTestEntity> cache1 = null;

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
		if (cache1 != null)
			cache1.close();
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

		cache1 = mache(String.class, CassandraTestEntity.class).cachedBy(guava()).backedBy(cassandra()
				.withCluster(Cluster.builder().withClusterName("BluePrint")
						.withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM))
						.withRetryPolicy(DefaultRetryPolicy.INSTANCE)
						.withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
						.addContactPoint(mapParams.get("cassandra.server.ip.address")).withPort(9042).build())
						.withKeyspace(mapParams.get("keyspace.name")).withSchemaOptions(SchemaOptions.CREATE_SCHEMA_IF_NEEDED)
				.build()).withMessaging(kafkaProvisioner).macheUp();
	}
}