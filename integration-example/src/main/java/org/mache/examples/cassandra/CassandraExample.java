package org.mache.examples.cassandra;

import com.datastax.driver.core.Cluster;
import org.mache.*;
import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;
import org.mache.events.integration.RabbitMQFactory;

import javax.jms.JMSException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mache.SchemaOptions.CREATEANDDROPSCHEMA;

/**
 * Created by jbowkett on 17/07/15.
 */
public class CassandraExample {
  
  protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
  
  protected ExCache<String, Message> exampleCache() throws Exception {
    final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
    final Cluster cluster = getCluster();
    final CassandraCacheLoader<String, Message> cacheLoader = getCacheLoader(keySpace, cluster);
    final CacheFactory cacheFactory = getCacheFactory();
    return cacheFactory.createCache(cacheLoader);
  }

  private Cluster getCluster() {
    System.out.println("Connecting to Cassandra cluster...");
    final Cluster cluster = CassandraCacheLoader.connect("10.28.1.140", "BluePrint", 9042);
    System.out.println("Connected.");
    return cluster;
  }


  private CassandraCacheLoader<String, Message> getCacheLoader(String keySpace, Cluster cluster) throws Exception {
    System.out.println("Creating cache loader with keyspace:["+keySpace+"]");
    final CassandraCacheLoader<String, Message> cacheLoader = new CassandraCacheLoader<>(
        Message.class,
        cluster, CREATEANDDROPSCHEMA,
        keySpace);
    System.out.println("CacheLoader created.");
    return cacheLoader;
  }

  private CacheFactory getCacheFactory() throws JMSException, IOException {
    System.out.println("Creating CacheFactory...");
    final MQConfiguration mqConfiguration = () -> "testTopic";
    final CacheThingFactory cacheThingFactory = new CacheThingFactory();
    final String LOCAL_MQ = "localhost";
    final MQFactory mqFactory = new RabbitMQFactory(LOCAL_MQ);
    final CacheFactoryImpl cacheFactory = new CacheFactoryImpl(mqFactory, mqConfiguration, cacheThingFactory);
    System.out.println("Cache Factory Created.");
    return cacheFactory;
  }
}
