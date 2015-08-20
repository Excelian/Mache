package com.excelian.mache.mongo;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.excelian.mache.core.NoRunningMongoDbForTests;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoConnector {

    private static final Logger LOG = LoggerFactory.getLogger(MongoConnector.class);

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Test
    @IgnoreIf(condition = NoRunningMongoDbForTests.class)
    public void connectsToTheCluster() throws Exception {

        MongoClient mongoClient = new MongoClient(new NoRunningMongoDbForTests().HostName(), 27017);
        MongoIterable<String> strings = mongoClient.listDatabaseNames();
        MongoCursor<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            LOG.info("Database: {}", iterator.next());
        }

        MongoDatabase db = mongoClient.getDatabase(strings.iterator().next());
        String name = db.getName();
        LOG.info("Got: {}", name);

        mongoClient.close();
    }
}
