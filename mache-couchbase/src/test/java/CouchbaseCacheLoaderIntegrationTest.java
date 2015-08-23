import com.codeaffine.test.ConditionalIgnoreRule;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.excelian.mache.core.MacheImpl;
import com.excelian.mache.core.NoRunningCouchbaseDbForTests;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.CouchbaseCacheLoader;
import com.excelian.mache.couchbase.CouchbaseConfig;
import com.google.common.cache.CacheLoader;
import org.junit.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.Collections;
import java.util.Objects;

import static org.junit.Assert.*;

@ConditionalIgnoreRule.IgnoreIf(condition = NoRunningCouchbaseDbForTests.class)
@Ignore
public class CouchbaseCacheLoaderIntegrationTest {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private static final String BUCKET = "couchbase-test";
    private static final String EXCELIAN_ADMIN = "Administrator";
    private static final String PASSWORD = "password";
    private static final double DELTA = 0.000001;
    private static final String EXCELIAN_COUCHBASE = "10.28.1.140";

    private MacheImpl<String, TestEntity> cache;

    @Before
    public void setup() {
        cache = new MacheImpl<>(new CouchbaseCacheLoader<>(CouchbaseConfig.builder()
                .withServerAddresses(Collections.singletonList(EXCELIAN_COUCHBASE))
                .withCouchbaseEnvironment(DefaultCouchbaseEnvironment.create())
                .withAdminUser(EXCELIAN_ADMIN)
                .withAdminPassword(PASSWORD)
                .withBucketName(BUCKET)
                .withSchemaOptions(SchemaOptions.CREATEANDDROPSCHEMA)
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

    @Test(expected = CacheLoader.InvalidCacheLoadException.class)
    public void canRemove() throws Throwable {
        TestEntity test2 = new TestEntity("test2", "FXRATE", 0.92);
        cache.put("test2", test2);
        assertEquals(test2, cache.get("test2"));
        cache.remove("test2");
        cache.get("test2");
    }

    @Test
    public void canOverwriteValue() throws Throwable {
        cache.put("test3", new TestEntity("test3", "FXRATE", 3.93));
        cache.put("test3", new TestEntity("test3", "FXRATE", 0.93));
        assertEquals(0.93, cache.get("test3").value, DELTA);
    }


    @Test
    @SuppressWarnings("unchecked")
    public void canGetDriver() {
        CouchbaseCacheLoader<String, Object> loader = (CouchbaseCacheLoader) cache.getCacheLoader();
        loader.create();
        Cluster cluster = loader.getDriverSession();
        assertNotNull(cluster);
        loader.close();
        assertNull(loader.getDriverSession());
    }

    @Document
    public static class TestEntity {
        @Id
        String key;

        String type;

        double value;

        public TestEntity(String key, String type, double value) {
            this.key = key;
            this.type = type;
            this.value = value;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            TestEntity that = (TestEntity) other;
            return Objects.equals(value, that.value)
                    && Objects.equals(key, that.key)
                    && Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, type, value);
        }
    }

}

