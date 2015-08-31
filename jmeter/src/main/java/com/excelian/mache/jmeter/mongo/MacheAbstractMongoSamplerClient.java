package com.excelian.mache.jmeter.mongo;

import com.excelian.mache.builder.MacheBuilder;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.events.integration.builder.ActiveMQMessagingProvisioner;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import com.excelian.mache.mongo.builder.MongoDBProvisioner;
import com.mongodb.ServerAddress;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import java.util.Map;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.mongo.builder.MongoDBProvisioner.mongodb;

@SuppressWarnings("serial")
public abstract class MacheAbstractMongoSamplerClient extends
        MacheAbstractJavaSamplerClient {

    protected Mache<String, MongoTestEntity> cache1 = null;

    @Override
    public void setupTest(JavaSamplerContext context) {
        Map<String, String> mapParams = extractParameters(context);
        getLogger().info("mache setupTest started  \n");

        try {
            createCache(mapParams);
            getLogger().info("mache setupTest completed. ");

            MongoTestEntity e = initMongoEntity(mapParams);
            cache1.put(e.pkString, e);

            getLogger().info("cache connection completed. ");
        } catch (Exception e) {
            getLogger().error("mache error building factory", e);
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
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument("mongo.server.ip.address", "10.28.1.140");
        defaultParameters.addArgument("activemq.connection", "vm://localhost");
        defaultParameters.addArgument("entity.key", "K${loopCounter}");
        defaultParameters.addArgument("entity.value", "V${loopCounter}");
        defaultParameters.addArgument("keyspace.name", "JMeterReadThrough");
        return defaultParameters;
    }

    protected void createCache(Map<String, String> mapParams)
            throws Exception {
        cache1 = mache(String.class, MongoTestEntity.class)
                .backedBy(mongodb()
                        .withSeeds(new ServerAddress(mapParams.get("mongo.server.ip.address"), 27017))
                        .withDatabase(mapParams.get("keyspace.name"))
                        .withSchemaOptions(SchemaOptions.CREATE_AND_DROP_SCHEMA)
                        .build())
                .withMessaging(ActiveMQMessagingProvisioner.activemq()
                        .withTopic("testTopic")
                        .withConnectionFactory(new ActiveMQConnectionFactory(mapParams.get("activemq.connection")))
                        .build())
                .macheUp();
    }

    protected MongoTestEntity initMongoEntity(Map<String, String> mapParams) {
        return new MongoTestEntity(mapParams.get("entity.key"), mapParams.get("entity.value"));
    }
}