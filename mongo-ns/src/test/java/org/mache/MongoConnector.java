package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.junit.Rule;
import org.junit.Test;

public class MongoConnector {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Test
    @IgnoreIf(condition = NoRunningMongoDbForTests.class)
    public void connectsToTheCluster() throws Exception {

        MongoClient mongoClient = new MongoClient(NoRunningMongoDbForTests.HostName(), 27017);
        MongoIterable<String> strings = mongoClient.listDatabaseNames();
        MongoCursor<String> iterator = strings.iterator();
        while (iterator.hasNext()) {
            System.out.println("Database:" + iterator.next());
        }

        MongoDatabase db = mongoClient.getDatabase(strings.iterator().next());
        String name = db.getName();
        System.out.println("Got:" + name);

        mongoClient.close();
    }
}
