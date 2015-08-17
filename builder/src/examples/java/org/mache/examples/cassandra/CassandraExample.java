package org.mache.examples.cassandra;

import org.mache.*;
import org.mache.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mache.SchemaOptions.CREATEANDDROPSCHEMA;
import static org.mache.builder.Builder.mache;
import static org.mache.builder.Builder.namedCluster;
import static org.mache.builder.Builder.server;

/**
 * Created by jbowkett on 17/07/15.
 */
public class CassandraExample implements Example<CassandraAnnotatedMessage> {
  
  protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  @Override
  public ExCache<String, CassandraAnnotatedMessage> exampleCache() {
    final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
    return mache()
        .backedByCassandra()
        .at(server("10.28.1.140", 9042))
        .with(namedCluster("BluePrint"))
        .withKeyspace(keySpace)
        .toStore(CassandraAnnotatedMessage.class)
        .withPolicy(CREATEANDDROPSCHEMA)
        .withNoMessaging().macheUp();
  }
}
