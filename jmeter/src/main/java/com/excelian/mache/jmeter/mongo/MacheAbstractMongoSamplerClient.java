package com.excelian.mache.jmeter.mongo;

import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.integration.ActiveMQFactory;
import com.excelian.mache.events.integration.DefaultActiveMqConfig;
import com.excelian.mache.jmeter.MacheAbstractJavaSamplerClient;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.excelian.mache.observable.MessageQueueObservableCacheFactory;
import com.excelian.mache.observable.ObservableCacheFactory;
import com.excelian.mache.observable.utils.UUIDUtils;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;

import javax.jms.JMSException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("serial")
public abstract class MacheAbstractMongoSamplerClient extends
    MacheAbstractJavaSamplerClient {

    protected ActiveMQFactory<String> mqFactory1 = null;
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
        mqFactory1 = new ActiveMQFactory<>(mapParams.get("activemq.connection"), new DefaultActiveMqConfig());
        ObservableCacheFactory<String, MongoTestEntity, Mongo> cacheFactory1 = new MessageQueueObservableCacheFactory<>(mqFactory1,
            getMQConfiguration(), new MacheFactory<>(), new UUIDUtils());
        cache1 = cacheFactory1
                .createCache(new MongoDBCacheLoader<>(
                        MongoTestEntity.class,
                        new CopyOnWriteArrayList<>(
                                Arrays.asList(new ServerAddress(mapParams
                                        .get("mongo.server.ip.address"), 27017))),
                        SchemaOptions.CREATEANDDROPSCHEMA, mapParams.get("keyspace.name")));
    }

    protected MongoTestEntity initMongoEntity(Map<String, String> mapParams) {
        return new MongoTestEntity(mapParams.get("entity.key"), mapParams.get("entity.value"));
    }
}