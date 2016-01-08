package com.excelian.mache.examples;

import com.datastax.driver.core.Cluster;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;

/**
 * Created by neil.avery on 07/01/2016.
 */
public class JsonCassandraSpike {

    /**
     * CQL Commands:
     *
//     CREATE KEYSPACE "mache_json"
//     WITH replication = {'class' : 'SimpleStrategy', 'replication_factor': 1};

//  CREATE TABLE users (
//    id text PRIMARY KEY,
//    age int,
//    state text
//  );

//  INSERT INTO users (id, age, state) VALUES ('user123', 42, 'TX');
//  INSERT INTO users JSON '{"id": "userJ123", "age": 42, "state": "TX"}';
//  select * from users;
//  DELETE from users WHERE id = 'user123';
//  TRUNCATE users;

//  SELECT JSON * FROM users;
//  SELECT * FROM users;
//  SELECT JSON * FROM users WHERE id = 'userJ123';


     * @param commandLine
     * @throws Exception
     */

    public static void main(String... commandLine) throws Exception {

        try {
            Mache<String, String> cache = exampleCache();
            String userJson = "{\"id\": \"user1234\", \"age\": 42, \"state\": \"TX\"}";
            System.out.println("WriteJSON:" + userJson);
            cache.put("users.user1234", userJson);
            String result = cache.get("users.user1234");
            System.out.println("ReadBackJSON:" + result);
            System.out.println("Test Pass:" + userJson.equals(result));
            assert userJson.equals(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Mache<String, String> exampleCache() throws Exception {
            final String keySpace = "mache-json";
            return mache(String.class, String.class)
                    .backedBy(cassandra()
                            .withCluster(Cluster.builder()
                                    .addContactPoint("10.28.0.111")
                                    .withPort(9042)
                                    .withClusterName("Test Cluster").build())
                            .withKeyspace(keySpace)
                            .withSchemaOptions(SchemaOptions.USE_EXISTING_SCHEMA).build())
                    .withNoMessaging()
                    .macheUp();
    }
}
