package com.excelian.mache.events.integration;

/**
 * Encapuslates Kafak config.
 */
public class KafkaMqConfig {

    private final String autoCommitInterval;
    private final String kafkaSerializerClass;
    private final String offsetReset;
    private final String requiredAckCount;
    private final int shutdownTimeoutSecs;
    private final String zkHost;
    private final String zkConsumerPort;
    private final String zkProducerPort;
    private final String zkSessionTimeout;
    private final String zkSyncTime;

    /**
     * Constructor.
     * @param autoCommitInterval - auto commit interval
     * @param kafkaSerializerClass - kafka Serializer Class
     * @param offsetReset - offset Reset
     * @param requiredAckCount - required Ack Count
     * @param shutdownTimeoutSecs - shutdown Timeout Secs
     * @param zkHost - Zookeeper Host
     * @param zkConsumerPort - Zookeper Consumer Port
     * @param zkProducerPort - Zookeeper Producer Port
     * @param zkSessionTimeout - Zookeeper Session Timeout
     * @param zkSyncTime - Zookeper Sync Time
     */
    KafkaMqConfig(String autoCommitInterval, String kafkaSerializerClass, String offsetReset,
                  String requiredAckCount, int shutdownTimeoutSecs, String zkHost, String zkConsumerPort,
                  String zkProducerPort, String zkSessionTimeout, String zkSyncTime) {
        this.autoCommitInterval = autoCommitInterval;
        this.kafkaSerializerClass = kafkaSerializerClass;
        this.offsetReset = offsetReset;
        this.requiredAckCount = requiredAckCount;
        this.shutdownTimeoutSecs = shutdownTimeoutSecs;
        this.zkHost = zkHost;
        this.zkConsumerPort = zkConsumerPort;
        this.zkProducerPort = zkProducerPort;
        this.zkSessionTimeout = zkSessionTimeout;
        this.zkSyncTime = zkSyncTime;
    }

    public int getShutdownTimeoutSeconds() {
        return shutdownTimeoutSecs;
    }

    public String getZookeeperConnectionString(String port) {
        return zkHost + ":" + port;
    }

    public String getZookeeperProducerPort() {
        return zkProducerPort;
    }

    public String getZookeeperConsumerPort() {
        return zkConsumerPort;
    }

    public String getOffsetReset() {
        return offsetReset;
    }

    public String getAutoCommitIntervalMilliseconds() {
        return autoCommitInterval;
    }

    public String getZookeeperSynchronisationTimeMilliseconds() {
        return zkSyncTime;
    }

    public String getZookeeperSessionTimeoutMilliseconds() {
        return zkSessionTimeout;
    }

    public String getSerializerClassName() {
        return kafkaSerializerClass;
    }

    public String getRequiredAcks() {
        return requiredAckCount;
    }

    /**
     * Builds the Kafka config.
     */
    public static class KafkaMqConfigBuilder {
        private String autoCommitInterval = "50";
        private String kafkaSerializerClass = "kafka.serializer.StringEncoder";
        private String offsetReset = "largest";
        private String requiredAckCount = "1";
        private int shutdownTimeoutSecs = 500;
        private String zkHost = "localhost";
        private String zkConsumerPort = "2181";
        private String zkProducerPort = "9092";
        private String zkSessionTimeout = "1000000";
        private String zkSyncTime = "20000";

        public static KafkaMqConfigBuilder builder() {
            return new KafkaMqConfigBuilder();
        }

        public KafkaMqConfigBuilder withAutoCommitInterval(String autoCommitInterval) {
            this.autoCommitInterval = autoCommitInterval;
            return this;
        }

        public KafkaMqConfigBuilder withKafkaSerializerClass(String kafkaSerializerClass) {
            this.kafkaSerializerClass = kafkaSerializerClass;
            return this;
        }

        public KafkaMqConfigBuilder withOffsetReset(String offsetReset) {
            this.offsetReset = offsetReset;
            return this;
        }

        public KafkaMqConfigBuilder withRequiredAckCount(String requiredAckCount) {
            this.requiredAckCount = requiredAckCount;
            return this;
        }

        public KafkaMqConfigBuilder withShutdownTimeoutSecs(int shutdownTimeoutSecs) {
            this.shutdownTimeoutSecs = shutdownTimeoutSecs;
            return this;
        }

        public KafkaMqConfigBuilder withZkHost(String zkHost) {
            this.zkHost = zkHost;
            return this;
        }

        public KafkaMqConfigBuilder withZkConsumerPort(String zkConsumerPort) {
            this.zkConsumerPort = zkConsumerPort;
            return this;
        }

        public KafkaMqConfigBuilder withZkProducerPort(String zkProducerPort) {
            this.zkProducerPort = zkProducerPort;
            return this;
        }

        public KafkaMqConfigBuilder withZkSessionTimeout(String zkSessionTimeout) {
            this.zkSessionTimeout = zkSessionTimeout;
            return this;
        }

        public KafkaMqConfigBuilder withZkSyncTime(String zkSyncTime) {
            this.zkSyncTime = zkSyncTime;
            return this;
        }

        public KafkaMqConfig build() {
            return new KafkaMqConfig(autoCommitInterval, kafkaSerializerClass, offsetReset, requiredAckCount,
                    shutdownTimeoutSecs, zkHost, zkConsumerPort, zkProducerPort, zkSessionTimeout, zkSyncTime);
        }
    }
}
