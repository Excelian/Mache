package com.excelian.mache.events.integration;

/**
 * Created by jbowkett on 21/08/2015.
 */
public class DefaultKafkaMqConfig implements KafkaMqConfig {

    private static final String AUTOCOMMIT_INTERVAL_MS = "50";
    private static final String KAFKA_SERIALIZER_CLASS = "kafka.serializer.StringEncoder";
    private static final String OFFSET_RESET = "largest";
    private static final String REQUIRED_ACK_COUNT = "1";
    private static final int SHUTDOWN_TIMEOUT_SECS = 5;
    private static final String ZK_CONSUMER_PORT = "2181";
    private static final String ZK_PRODUCER_PORT = "9092";
    private static final String ZK_SESSION_TIMEOUT_MS = "8000";
    private static final String ZK_SYNC_TIME_MS = "100";

    public int getShutdownTimeoutSeconds() {
        return SHUTDOWN_TIMEOUT_SECS;
    }

    public String getZookeeperConnectionString(String zooKeeper, String port) {
        return zooKeeper + ":" + port;
    }

    public String getZookeeperProducerPort() {
        return ZK_PRODUCER_PORT;
    }

    public String getZookeeperConsumerPort() {
        return ZK_CONSUMER_PORT;
    }

    public String getOffsetReset() {
        return OFFSET_RESET;
    }

    public String getAutoCommitIntervalMilliseconds() {
        return AUTOCOMMIT_INTERVAL_MS;
    }

    public String getZookeeperSynchronisationTimeMilliseconds() {
        return ZK_SYNC_TIME_MS;
    }

    public String getZookeeperSessionTimeoutMilliseconds() {
        return ZK_SESSION_TIMEOUT_MS;
    }

    public String getSerializerClassName() {
        return KAFKA_SERIALIZER_CLASS;
    }

    public String getRequiredAcks() {
        return REQUIRED_ACK_COUNT;
    }

}
