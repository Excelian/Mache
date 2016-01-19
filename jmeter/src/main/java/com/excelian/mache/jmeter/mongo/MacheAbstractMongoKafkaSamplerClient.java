package com.excelian.mache.jmeter.mongo;

import com.excelian.mache.builder.storage.ConnectionContext;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.events.integration.KafkaMqConfig;
import com.excelian.mache.events.integration.builder.KafkaMessagingProvisioner;
import com.mongodb.ServerAddress;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongoConnectionContext;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;

import java.util.List;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class MacheAbstractMongoKafkaSamplerClient extends AbstractMongoSamplerClient {

    protected ConnectionContext<List<ServerAddress>> connectionContext;
    protected Mache<String, MongoTestEntity> cache1 = null;

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

        if(connectionContext!=null) {
            try {
                connectionContext.close();
            } catch (Exception e) {
                getLogger().error("mache disconnection from mongo", e);
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
        final KafkaMessagingProvisioner kafkaProvisioner =
            KafkaMessagingProvisioner.kafka()
                .withKafkaMqConfig(KafkaMqConfig.KafkaMqConfigBuilder.builder()
                        .withZkHost(mapParams.get("kafka.connection")).build())
                .withTopic(mapParams.get("kafka.topic"));

        connectionContext=mongoConnectionContext(new ServerAddress(mapParams.get("mongo.server.ip.address"), 27017));

        cache1 = mache(String.class, com.excelian.mache.jmeter.mongo.MongoTestEntity.class)
            .backedBy(mongodb()
                .withConnectionContext(connectionContext)
                .withDatabase(mapParams.get("keyspace.name"))
                .withSchemaOptions(SchemaOptions.CREATE_SCHEMA_IF_NEEDED)
                .build())
            .withMessaging(kafkaProvisioner)
            .macheUp();


    }
}