package com.excelian.mache.couchbase;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.builder.NoMessagingProvisioner;
import com.excelian.mache.core.Mache;
import com.excelian.mache.couchbase.builder.CouchbaseProvisioner;
import junit.extensions.TestSetup;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.couchbase.client.java.cluster.DefaultBucketSettings.builder;
import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.caffeine.CaffeineMacheProvisioner.caffeine;
import static com.excelian.mache.core.SchemaOptions.CREATE_AND_DROP_SCHEMA;
import static com.excelian.mache.couchbase.builder.CouchbaseProvisioner.couchbase;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ConditionalIgnoreRule.IgnoreIf(condition = NoRunningCouchbaseDbForTests.class)
public class CouchbaseCacheLoaderIntegrationTest {

    private static final String BUCKET = "couchbase-test";
    private static final String ADMIN_USER = "Administrator";
    private static final String PASSWORD = "password";
    private static final double EPSILON = 0.000001;
    private static final String COUCHBASE_HOST = new NoRunningCouchbaseDbForTests().getHost();

    private static final CouchbaseEnvironment COUCHBASE_ENVIRONMENT =
        DefaultCouchbaseEnvironment.builder().connectTimeout(SECONDS.toMillis(100)).build();
    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    private Mache<String, TestEntity> cache;

    @Before
    public void setup() throws Exception {
        cache = mache(String.class, TestEntity.class)
                .cachedBy(caffeine())
                .storedIn(couchbase()
                        .withBucketSettings(builder().name(BUCKET).quota(150).build())
                        .withCouchbaseEnvironment(COUCHBASE_ENVIRONMENT)
                        .withAdminDetails(ADMIN_USER, PASSWORD)
                        .withNodes(COUCHBASE_HOST)
                        .withSchemaOptions(CREATE_AND_DROP_SCHEMA)
                        .build())
                .withMessaging(new NoMessagingProvisioner<>())
                .macheUp();
    }

    @After
    public void tearDown() {
        if (cache != null) {
            cache.close();
        }
    }

    @Test
    public void canPutAndGetValue() throws Throwable {
        cache.put("test1", new TestEntity("test1", "FXRATE", 0.91));
        assertEquals(0.91, cache.get("test1").value, EPSILON);
    }

    @Test
    public void canRemove() throws Throwable {
        TestEntity test2 = new TestEntity("test2", "FXRATE", 0.92);
        cache.put("test2", test2);
        assertEquals(test2, cache.get("test2"));
        cache.remove("test2");
        TestEntity removed = cache.get("test2");
        assertNull(removed);
    }

    @Test
    public void canOverwriteValue() throws Throwable {
        cache.put("test3", new TestEntity("test3", "FXRATE", 3.93));
        cache.put("test3", new TestEntity("test3", "FXRATE", 0.93));
        assertEquals(0.93, cache.get("test3").value, EPSILON);
    }

}
