package org.mache.examples.examples.mongo;

import org.mache.*;
import org.mache.examples.examples.Example;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mache.SchemaOptions.CREATEANDDROPSCHEMA;
import static org.mache.builder.Builder.mache;
import static org.mache.builder.Builder.server;

/**
 * Created by jbowkett on 17/07/15.
 */
public class MongoExample implements Example<MongoAnnotatedMessage> {
  
  protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  @Override
  public ExCache<String, MongoAnnotatedMessage> exampleCache() {
    final String keySpace = "NoSQL_MacheClient_Test_" + DATE_FORMAT.format(new Date());
    return mache()
        .backedByMongo()
        .at(server("10.28.1.140", 9042))
        .withKeyspace(keySpace)
        .toStore(MongoAnnotatedMessage.class)
        .withPolicy(CREATEANDDROPSCHEMA)
        .withNoMessaging()
        .macheUp();
  }

}

