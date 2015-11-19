package com.excelian.mache.jmeter.couch;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.events.integration.KafkaMqConfig;
import com.excelian.mache.events.integration.builder.KafkaMessagingProvisioner;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.util.Map;

import static com.couchbase.client.java.cluster.DefaultBucketSettings.builder;
import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.couchbase.builder.CouchbaseProvisioner.couchbase;

@SuppressWarnings("serial")
public abstract class MacheAbstractCouchKafkaSamplerClient extends AbstractCouchSamplerClient {

    protected Mache<String, CouchTestEntity> cache1 = null;

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
    }

    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = super.getDefaultParameters();

        defaultParameters.addArgument("kafka.connection", "192.168.3.4");
        defaultParameters.addArgument("kafka.topic", "Kafka_JmeterCassandraTest");
        return defaultParameters;
    }

    protected void createCache(Map<String, String> mapParams) throws Exception {
        final KafkaMessagingProvisioner kafkaProvisioner =
            KafkaMessagingProvisioner.kafka()
                .withKafkaMqConfig(KafkaMqConfig.KafkaMqConfigBuilder.builder()
                    .withZkHost(mapParams.get("kafka.connection")).build())
                .withTopic(mapParams.get("kafka.topic"));

        final String keySpace = mapParams.get("keyspace.name");
        final String couchServer = mapParams.get("couch.server.ip.address");

        final Mache<String, CouchTestEntity> mache = mache(String.class, CouchTestEntity.class)
            .backedBy(couchbase()
                .withBucketSettings(builder().name(keySpace).quota(150).build())
                .withNodes(couchServer)
                .withSchemaOptions(SchemaOptions.CREATE_SCHEMA_IF_NEEDED)
                .create())
            .withMessaging(kafkaProvisioner)
            .macheUp();


    }
}