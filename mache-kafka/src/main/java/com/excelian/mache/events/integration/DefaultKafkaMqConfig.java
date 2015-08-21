package com.excelian.mache.events.integration;

/**
 * Created by jbowkett on 21/08/2015.
 */
public class DefaultKafkaMqConfig implements KafkaMqConfig {

    public int getShutdownTimeoutSeconds() {
        return 5;
    }

    public String getZookeeperConnectionString(String zooKeeper, String ZK_PORT) {
        return zooKeeper + ":" + ZK_PORT;
    }

    public String getZookeeperProducerPort() {
        return "9092";
    }

    public String getZookeeperConsumerPort() {
        return "2181";
    }

    public String getOffsetReset() {
        return "largest";
    }

    public String getAutoCommitIntervalMilliseconds() {
        return "50";
    }

    public String getZookeeperSynchronisationTimeMilliseconds() {
        return "100";
    }

    public String getZookeeperSessionTimeoutMilliseconds() {
        return "8000";
    }

    public String getSerializerClassName() {
        return "kafka.serializer.StringEncoder";
    }

    public String getRequiredAcks() {
        return "1";
    }

}
