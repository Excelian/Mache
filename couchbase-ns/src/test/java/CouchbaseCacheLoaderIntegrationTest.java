import com.codeaffine.test.ConditionalIgnoreRule;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.google.common.cache.CacheLoader;
import org.junit.*;
import org.mache.CacheThing;
import org.mache.NotRunningInExcelian;
import org.mache.SchemaOptions;
import org.mache.couchbase.CouchbaseCacheLoader;
import org.mache.couchbase.CouchbaseConfig;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;

import java.util.Collections;
import java.util.Objects;

import static org.junit.Assert.*;

//@ConditionalIgnoreRule.IgnoreIf(condition = NotRunningInExcelian.class)
@Ignore
public class CouchbaseCacheLoaderIntegrationTest {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    private static final String BUCKET = "couchbase-test";
    private static final String EXCELIAN_ADMIN = "Administrator";
    //private static final String LOCAL_ADMIN = "admin";
    private static final String PASSWORD = "password";
    private static final double DELTA = 0.000001;
    private static final String EXCELIAN_COUCHBASE = "10.28.1.140";
    //private static final String LOCAL_COUCHBASE = "192.168.56.100";

    private CacheThing<String, TestEntity> cache;

    @Before
    public void setup() {
        cache = new CacheThing<>(new CouchbaseCacheLoader<>(CouchbaseConfig.builder()
                .withServerAdresses(Collections.singletonList(EXCELIAN_COUCHBASE))
                //.withServerAdresses(Collections.singletonList(LOCAL_COUCHBASE))
                .withCouchbaseEnvironment(DefaultCouchbaseEnvironment.create())
                .withAdminUser(EXCELIAN_ADMIN)
                //.withAdminUser(LOCAL_ADMIN)
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
        loader.create(null, null);
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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestEntity that = (TestEntity) o;
            return Objects.equals(value, that.value) &&
                    Objects.equals(key, that.key) &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, type, value);
        }
    }

}

