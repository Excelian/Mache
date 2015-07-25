import com.codeaffine.test.ConditionalIgnoreRule;
import com.codeaffine.test.ConditionalIgnoreRule.IgnoreIf;
import com.couchbase.client.CouchbaseClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mache.CacheThing;
import org.mache.NotRunningInExcelian;
import org.mache.couchbase.CouchbaseCacheLoader;
import org.mache.couchbase.CouchbaseConfig;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@IgnoreIf(condition = NotRunningInExcelian.class)
public class CouchbaseCacheIntegrationTest {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    public static final double DELTA = 0.000001;
    private CacheThing<String, TestEntity> cache;

    @Before
    public void setup() {
        cache = new CacheThing<>(new CouchbaseCacheLoader<>(CouchbaseConfig.builder()
                .withServerAdresses(Arrays.asList("http://10.28.1.140:8091/pools"))
                .withAdminUser("Administrator")
                .withAdminPassword("password")
                .withBucketName("couchbase-test")
                .withCacheType(TestEntity.class).build()));
    }

    @After
    public void tearDown() {
        cache.close();
    }

    @Test
    public void canPutAndGetValue() throws Throwable {
        cache.put("test1", new TestEntity("test1", "FXRATE", 0.91));
        assertEquals(0.91, cache.get("test1").value, DELTA);
    }

    @Test
    public void canRemove() throws Throwable {
        /* FIXME - odd behaviour. I'm seeing that if you put a breakpoint at the cache.get("test2") and when you
           hit it, evalutate (ALT-F8) the cache.get("test2") then it will actually return a value.
           If you don't evaluate everything is fine. */

        int exceptionCount = 0;
        final int total = 100;

        for (int i = 0; i < total; i++) {
            cache.put("test2", new TestEntity("test2", "FXRATE", 0.92));
            cache.remove("test2");
            try {
                cache.get("test2");
            } catch (Exception exp) {
                exceptionCount++;
            }
        }
        assertEquals(total, exceptionCount);
    }

    @Test
    public void canOverwriteValue() throws Throwable {
        cache.put("test3", new TestEntity("test3", "FXRATE", 3.93));
        cache.put("test3", new TestEntity("test3", "FXRATE", 0.93));
        assertEquals(0.93, cache.get("test3").value, DELTA);
    }


    @Test
    public void canGetDriver() {
        CouchbaseCacheLoader loader = (CouchbaseCacheLoader) cache.getCacheLoader();
        loader.create(null, null);
        CouchbaseClient client = loader.getDriverSession();
        assertEquals(1, client.getAvailableServers().size());
    }

    @Document
    public static class TestEntity {
        @Id
        String key;

        @Field
        String type;

        @Field
        double value;

        public TestEntity(String key, String type, double value) {
            this.key = key;
            this.type = type;
            this.value = value;
        }
    }

}

