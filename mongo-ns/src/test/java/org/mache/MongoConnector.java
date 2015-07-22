package org.mache;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.junit.Rule;
import org.junit.Test;

/**
 * Created by neil.avery on 27/05/2015.
 */
public class MongoConnector {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Test
    @IgnoreIf(condition = NotRunningInExcelian.class)
    public void connectsToTheCluster() throws Exception {
        MongoClient mongoClient = new MongoClient("10.28.1.140", 27017);
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
