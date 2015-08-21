package com.excelian.mache.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.policies.ReconnectionPolicy;

/**
 * Created by jbowkett on 21/08/2015.
 */
public interface CassandraConfig {


    ConsistencyLevel getConsistencyLevel();

    String getReplicationClass();

    int getReplicationFactor();

    ReconnectionPolicy getReconnectionPolicy();
}
