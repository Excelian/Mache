package com.excelian.mache.cassandra;

import com.codeaffine.test.ConditionalIgnoreRule;
import com.datastax.driver.core.Cluster;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.SchemaOptions;
import com.google.common.cache.CacheLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.*;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandra;
import static com.excelian.mache.cassandra.builder.CassandraProvisioner.cassandraConnectionContext;
import static com.excelian.mache.guava.GuavaMacheProvisioner.guava;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.Serializable;
import java.util.Date;

@ConditionalIgnoreRule.IgnoreIf(condition = NoRunningCassandraDbForTests.class)
public class CassandraCacheLoaderIntegrationTest {

    protected static String keySpace = "NoSQL_Nearside_Test_" + new Date().toString();

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();
    private Mache<String, TestEntity> mache;

    @Before
    public void setUp() throws Exception {
        mache = getMache(String.class, TestEntity.class, SchemaOptions.CREATE_AND_DROP_SCHEMA);
    }

    @After
    public void tearDown() throws Exception {
        if (mache != null) {
            mache.close();
        }
    }

    private static <K, V> Mache<K, V> getMache(Class<K> keyType,
                                               Class<V> valueType,
                                               SchemaOptions schemaOptions) throws Exception {
        return mache(keyType, valueType)
                .cachedBy(guava())
                .storedIn(cassandra()
                    .withCluster(Cluster.builder()
                                        .withClusterName("BluePrint")
                                        .addContactPoint(CASSANDRA_BLUEPRINT.getHost())
                                        .withPort(9042))
                                    .withKeyspace(keySpace)
                                    .withSchemaOptions(schemaOptions)
                                    .build())
                                .withNoMessaging()
                                .macheUp();
    }

    @Test
    public void testPut() throws Exception {
        mache.put("value-yay", new TestEntity("value-yay"));
        TestEntity test = mache.get("value-yay");
        assertNotNull("Expected object to be retrieved from cache", test);
        assertEquals("value-yay", test.pkString);
    }

    @Test
    public void canPutTheSameItemAgainTest() throws Exception {
        mache.put("test-1", new TestEntity("test-1"));
        mache.put("test-1", new TestEntity("test-1"));
        TestEntity test = mache.get("test-1");
        assertEquals("test-1", test.pkString);
    }

    @Test
    public void testReadCache() throws Exception {
        mache.put("test-2", new TestEntity("test-2"));
        TestEntity test = mache.get("test-2");
        assertEquals("test-2", test.pkString);
    }

    @Test(expected = CacheLoader.InvalidCacheLoadException.class)
    public void testRemove() throws Exception {
        String key = "rem-test-2";
        mache.put(key, new TestEntity(key));
        assertNotNull("Expected entry to be in cache prior to removal", mache.get(key));
        mache.remove(key);
        mache.get(key);
    }

    @Test
    public void testReadThrough() throws Exception {
        mache.put("test-2", new TestEntity("test-2"));
        mache.put("test-3", new TestEntity("test-3"));
        // replace the cache
        try (Mache<String, TestEntity> anothermache = getMache(String.class, TestEntity.class, connectionContext, SchemaOptions.CREATE_SCHEMA_IF_NEEDED)) {
            TestEntity test = anothermache.get("test-2");
            assertEquals("test-2", test.pkString);
        }
    }

    @Test
    public void testPutComposite() throws Exception {

        try (Mache<CompositeKey, TestEntityWithCompositeKey> compCache =
                     getMache(CompositeKey.class, TestEntityWithCompositeKey.class, connectionContext, SchemaOptions.CREATE_AND_DROP_SCHEMA)) {

            TestEntityWithCompositeKey value = new TestEntityWithCompositeKey("neil", "mac", "explorer");
            compCache.put(value.compositeKey, value);

            TestEntityWithCompositeKey testValue = compCache.get(value.compositeKey);
            assertEquals("neil", testValue.compositeKey.personId);
        }
    }

    @Table
    public static class TestEntity {
        @PrimaryKey
        String pkString = "yay";
        @Column
        private int firstInt = 1;
        @Column
        private double aDouble = 1.0;
        @Column(value = "mappedColumn")
        private String aString = "yay";

        public TestEntity(String pkString) {
            this.pkString = pkString;
        }
    }

    @Table
    public static class TestEntityWithCompositeKey {
        @Column
        private int firstInt = 1;
        @Column
        private double aDouble = 1.0;
        @Column(value = "mappedColumn")
        private String aString = "yay";
        @PrimaryKey
        private CompositeKey compositeKey = new CompositeKey("a", "b", "c");

        public TestEntityWithCompositeKey() {
        }

        public TestEntityWithCompositeKey(String person, String workstation, String app) {
            compositeKey = new CompositeKey(person, workstation, app);
        }
    }

    @PrimaryKeyClass
    public static class CompositeKey implements Serializable {

        @PrimaryKeyColumn(name = "person_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private String personId;
        @PrimaryKeyColumn(name = "wks_id", ordinal = 1, type = PrimaryKeyType.PARTITIONED)
        private String workstationId;
        @PrimaryKeyColumn(ordinal = 2, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
        private String application;

        public CompositeKey() {
        }

        public CompositeKey(String personId, String workstationId, String application) {
            this.personId = personId;
            this.workstationId = workstationId;
            this.application = application;
        }

        @Override
        public int hashCode() {
            return (personId + "-" + workstationId + "-" + application).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            CompositeKey o = (CompositeKey) obj;
            return (o.personId.equals(this.personId)
                    && o.application.equals(this.application)
                    && workstationId.equals(this.workstationId));
        }
    }
}