package org.mache.examples.mongo;

import com.mongodb.ServerAddress;
import org.mache.*;
import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;
import org.mache.events.integration.RabbitMQFactory;

import javax.jms.JMSException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mache.SchemaOptions.CREATEANDDROPSCHEMA;

/**
 * Created by jbowkett on 17/07/15.
 */
public class MongoExample implements AutoCloseable {
  
  protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
  private MongoDBCacheLoader<String, MongoAnnotatedMessage> cacheLoader;
  private MQFactory mqFactory;

  public ExCache<String, MongoAnnotatedMessage> exampleCache() throws IOException, JMSException {
    cacheLoader = getCacheLoader();
    mqFactory = getMqFactory();
    final CacheFactory cacheFactory = getCacheFactory(mqFactory);
    return cacheFactory.createCache(cacheLoader);
  }

  private MongoDBCacheLoader<String, MongoAnnotatedMessage> getCacheLoader() {
    final List<ServerAddress> serverAddresses = Collections.singletonList(new ServerAddress("10.28.1.140", 27017));
    final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
    return new MongoDBCacheLoader<>(
        MongoAnnotatedMessage.class,
        serverAddresses,
        CREATEANDDROPSCHEMA,
        keySpace);
  }


  private CacheFactory getCacheFactory(MQFactory mqFactory) throws IOException, JMSException {
    System.out.println("Creating CacheFactory...");
    final MQConfiguration mqConfiguration = () -> "testTopic";
    final CacheThingFactory cacheThingFactory = new CacheThingFactory();
    final CacheFactoryImpl cacheFactory = new CacheFactoryImpl(mqFactory, mqConfiguration, cacheThingFactory);
    System.out.println("Cache Factory Created.");
    return cacheFactory;
  }

  private MQFactory getMqFactory() throws JMSException, IOException {
    final String LOCAL_MQ = "localhost";
    return new RabbitMQFactory(LOCAL_MQ);
  }

  @Override
  public void close() {
    if(cacheLoader != null){
      cacheLoader.close();
    }
    if(mqFactory != null){
      try {
        mqFactory.close();
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
