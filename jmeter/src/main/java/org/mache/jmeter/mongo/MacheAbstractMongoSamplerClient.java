package org.mache.jmeter.mongo;

import com.mongodb.ServerAddress;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.mache.*;
import org.mache.events.MQConfiguration;
import org.mache.events.integration.ActiveMQFactory;
import org.mache.jmeter.MacheAbstractJavaSamplerClient;
import org.mache.utils.UUIDUtils;

import javax.jms.JMSException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("serial")
public abstract class MacheAbstractMongoSamplerClient extends
        MacheAbstractJavaSamplerClient {

    protected ActiveMQFactory mqFactory1 = null;
    protected ExCache<String, MongoTestEntity> cache1 = null;


    @Override
    public void setupTest(JavaSamplerContext context) {
        Map<String, String> mapParams = ExtractParameters(context);
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
        if (cache1 != null) cache1.close();
        if (mqFactory1 != null) mqFactory1.close();
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

    protected MQConfiguration getMQConfiguration() {
        return new MQConfiguration() {
            @Override
            public String getTopicName() {
                return "testTopic";
            }
        };
    }

    protected void createCache(Map<String, String> mapParams)
            throws JMSException {
        mqFactory1 = new ActiveMQFactory(mapParams.get("activemq.connection"));
        CacheFactoryImpl cacheFactory1 = new CacheFactoryImpl(mqFactory1,
                getMQConfiguration(), new CacheThingFactory(), new UUIDUtils());
        cache1 = cacheFactory1
                .createCache(new MongoDBCacheLoader<String, MongoTestEntity>(
                        MongoTestEntity.class,
                        new CopyOnWriteArrayList<>(
                                Arrays.asList(new ServerAddress(mapParams
                                        .get("mongo.server.ip.address"), 27017))),
                        SchemaOptions.CREATEANDDROPSCHEMA, mapParams.get("keyspace.name")));
    }

    protected MongoTestEntity initMongoEntity(Map<String, String> mapParams) {
        MongoTestEntity e = new MongoTestEntity(mapParams.get("entity.key"), mapParams.get("entity.value"));
        return e;
    }
}