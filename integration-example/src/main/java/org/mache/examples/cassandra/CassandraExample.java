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
public class CassandraExample implements AutoCloseable{
  
  protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
  private MQFactory mqFactory;
  private Cluster cluster;
  private CassandraCacheLoader<String, CassandraAnnotatedMessage> cacheLoader;

  public ExCache<String, CassandraAnnotatedMessage> exampleCache() throws IOException, JMSException {
    final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
    cluster = getCluster();
    cacheLoader = getCacheLoader(keySpace, cluster);
    mqFactory = getMqFactory();
    final CacheFactory cacheFactory = getCacheFactory(mqFactory);
    return cacheFactory.createCache(cacheLoader);
  }

  private Cluster getCluster() {
    System.out.println("Connecting to Cassandra cluster...");
    final Cluster cluster = CassandraCacheLoader.connect("10.28.1.140", "BluePrint", 9042);
    System.out.println("Connected.");
    return cluster;
  }


  private CassandraCacheLoader<String, CassandraAnnotatedMessage> getCacheLoader(String keySpace, Cluster cluster){
    System.out.println("Creating cache loader with keyspace:["+keySpace+"]");
    final CassandraCacheLoader<String, CassandraAnnotatedMessage> cacheLoader = new CassandraCacheLoader<>(
        CassandraAnnotatedMessage.class,
        cluster, CREATEANDDROPSCHEMA,
        keySpace);
    System.out.println("CacheLoader created.");
    return cacheLoader;
  }

  private CacheFactory getCacheFactory(MQFactory mqFactory) throws JMSException, IOException {
    System.out.println("Creating CacheFactory...");
    final CacheThingFactory cacheThingFactory = new CacheThingFactory();
    final MQConfiguration mqConfiguration = () -> "testTopic";
    final CacheFactoryImpl cacheFactory = new CacheFactoryImpl(mqFactory, mqConfiguration, cacheThingFactory);
    System.out.println("Cache Factory Created.");
    return cacheFactory;
  }

  private MQFactory getMqFactory() throws JMSException, IOException {
    final String LOCAL_MQ = "localhost";
    return new RabbitMQFactory(LOCAL_MQ);
  }

  @Override
  public void close(){
    if(cacheLoader != null){
      cacheLoader.close();
    }
    if(cluster != null){
      cluster.close();
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
