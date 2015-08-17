package org.mache.examples.examples.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Created by jbowkett on 17/07/15.
 */
@Document
public class MongoAnnotatedMessage {

  @Field
  private final String msg;

  @Id
  private final String primaryKey;

  public MongoAnnotatedMessage(String primaryKey, String msg) {
    this.msg = msg;
    this.primaryKey = primaryKey;
  }
  
  public String getPrimaryKey(){
    return primaryKey;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Message{");
    sb.append("msg='").append(msg).append('\'');
    sb.append(", primaryKey='").append(primaryKey).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
