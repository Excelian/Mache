package com.excelian.mache.cassandra;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;

/**
 * Created by jbowkett on 21/08/2015.
 */
public class DefaultCassandraConfig implements CassandraConfig {

    private static final int REPLICATION_FACTOR = 1; //Note: Travis only provides a single DSE node
    private static final String REPLICATION_CLASS = "SimpleStrategy";
    private static final long RECONNECTION_DELAY_MS = 100L;


    public ConsistencyLevel getConsistencyLevel() {
        return ConsistencyLevel.LOCAL_QUORUM;
    }

    public int getReplicationFactor() {
        return REPLICATION_FACTOR;
    }

    public String getReplicationClass() {
        return REPLICATION_CLASS;
    }

    public ConstantReconnectionPolicy getReconnectionPolicy() {
        return new ConstantReconnectionPolicy(getReconnectionDelayMs());
    }

    public static long getReconnectionDelayMs() {
        return RECONNECTION_DELAY_MS;
    }
}
