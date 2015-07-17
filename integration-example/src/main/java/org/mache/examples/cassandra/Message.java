package org.mache.examples.cassandra;

import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

/**
 * Created by jbowkett on 17/07/15.
 */
@Table
public class Message {

  private static final String userName = System.getProperty("user.name");

  private final String msg;

  @PrimaryKey
  private final String primaryKey;

  public Message(String primaryKey, String msg) {
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
