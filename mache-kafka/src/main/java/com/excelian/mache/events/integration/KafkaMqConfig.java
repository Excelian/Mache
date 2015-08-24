package com.excelian.mache.events.integration;


/**
 * Created by jbowkett on 21/08/2015.
 */
public interface KafkaMqConfig {
    int getShutdownTimeoutSeconds();

    String getZookeeperConsumerPort();

    String getZookeeperConnectionString(String zooKeeper, String zkPort);

    String getZookeeperSessionTimeoutMilliseconds();

    String getZookeeperSynchronisationTimeMilliseconds();

    String getAutoCommitIntervalMilliseconds();

    String getOffsetReset();

    String getZookeeperProducerPort();

    String getSerializerClassName();

    String getRequiredAcks();
}
