package org.mache;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ProtocolOptions;
import com.datastax.driver.core.Session;
import org.junit.Test;

/**
 * Created by neil.avery on 27/05/2015.
 */
public class CassandraConnectorTest {

    @Test
    public void connectsToTheCassandraCluster() throws Exception {
        Cluster cluster;
        Session session;

        // Connect to the cluster and keyspace "testKube"
        cluster = Cluster.builder().addContactPoint("10.28.1.140").withPort(9042).withClusterName("BluePrint").build();
        Metadata metadata = cluster.getMetadata();
        System.out.println("Clustername:" + metadata.getClusterName());
        System.out.println("Partitioner:" + metadata.getPartitioner());
        System.out.println("Hosts:" + metadata.getAllHosts());
        System.out.println("KeySpaces:" + metadata.getKeyspaces());

        session = cluster.connect("system");//system keyspace should always be present
        session.close();
    }
}
