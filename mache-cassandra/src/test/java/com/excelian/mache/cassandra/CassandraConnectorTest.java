package com.excelian.mache.cassandra;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;

public class CassandraConnectorTest {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraConnectorTest.class);

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Test
    @ConditionalIgnoreRule.IgnoreIf(condition = NoRunningCassandraDbForTests.class)
    public void connectsToTheCassandraCluster() throws Exception {
        Cluster cluster = Cluster.builder()
                .addContactPoint(new NoRunningCassandraDbForTests().getHost())
                .withPort(9042)
                .withClusterName("BluePrint")
                .build();
        Metadata metadata = cluster.getMetadata();
        LOG.info("Clustername:" + metadata.getClusterName());
        LOG.info("Partitioner:" + metadata.getPartitioner());
        LOG.info("Hosts:" + metadata.getAllHosts());
        LOG.info("KeySpaces:" + metadata.getKeyspaces());

        Session session = cluster.connect("system");//system keyspace should always be present
        assertNotNull(session);
        session.close();
    }
}