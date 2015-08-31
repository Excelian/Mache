import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.cluster.BucketSettings;
import com.couchbase.client.java.cluster.ClusterManager;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.couchbase.CouchbaseCacheLoader;
import com.excelian.mache.couchbase.CouchbaseConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@PrepareForTest(CouchbaseCluster.class)
@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
public class CouchbaseCacheLoaderMockedTest {

    CouchbaseConfig config;
    CouchbaseCacheLoader loader;
    CouchbaseCluster mockedCluster;
    ClusterManager mockedManager;

    @Before
    public void mockCouchbaseCluster() {
        PowerMockito.mockStatic(CouchbaseCluster.class);
        mockedCluster = mock(CouchbaseCluster.class);
        when(CouchbaseCluster.create(any(CouchbaseEnvironment.class),
                anyListOf(String.class))).thenReturn(mockedCluster);
        mockedManager = mock(ClusterManager.class);
        when(mockedCluster.clusterManager(anyString(), anyString())).thenReturn(mockedManager);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentWhenNotGivenConfig() throws Exception {
        new CouchbaseCacheLoader<>(null);
    }

    @Test
    public void shouldCreateConnection() throws Exception {
        givenCacheLoaderWith(SchemaOptions.USEEXISTINGSCHEMA);
        loader.create();
        thenClusterManagerAndBucketCreated(mockedCluster);
    }

    @Test
    public void shouldCreateBucket() throws Exception {
        givenCacheLoaderWith(SchemaOptions.CREATESCHEMAIFNEEDED);

        when(mockedManager.hasBucket(anyString())).thenReturn(false);
        when(mockedCluster.clusterManager(anyString(), anyString())).thenReturn(mockedManager);

        loader.create();

        thenBucketCreated();
        thenClusterManagerAndBucketCreated(mockedCluster);
    }

    @Test
    public void shouldDropAndCreateBucket() throws Exception {
        givenCacheLoaderWith(SchemaOptions.CREATEANDDROPSCHEMA);

        when(mockedManager.hasBucket(anyString())).thenAnswer(getAlternatingBooleanAnswer());
        when(mockedCluster.clusterManager(anyString(), anyString())).thenReturn(mockedManager);

        loader.create();

        thenBucketDropped();
        thenBucketCreated();
        thenClusterManagerAndBucketCreated(mockedCluster);
    }

    @Test
    public void shouldCloseCluster() throws Exception {
        givenCacheLoaderWith(SchemaOptions.USEEXISTINGSCHEMA);
        loader.create();
        loader.close();
        thenClusterClosed();
    }

    @Test
    public void shouldCloseClusterAndDropSchema() throws Exception {
        givenCacheLoaderWith(SchemaOptions.CREATEANDDROPSCHEMA);
        loader.create();
        when(mockedManager.hasBucket(anyString())).thenReturn(true);
        loader.close();
        thenClusterClosed();
        thenBucketDropped();
    }

    private void thenClusterClosed() {
        verify(mockedCluster).disconnect();
    }

    private void thenBucketCreated() {
        verify(mockedManager).insertBucket(
                any(BucketSettings.class), eq(CouchbaseCacheLoader.TIMEOUT), eq(TimeUnit.SECONDS));
    }

    private void thenBucketDropped() {
        verify(mockedManager).removeBucket(
                eq(config.getBucketName()), eq(CouchbaseCacheLoader.TIMEOUT), eq(TimeUnit.SECONDS));
    }

    private void thenClusterManagerAndBucketCreated(CouchbaseCluster mockedCluster) {
        verify(mockedCluster).clusterManager(config.getAdminUser(), config.getAdminPassword());
        verify(mockedCluster).openBucket("test", CouchbaseCacheLoader.TIMEOUT, TimeUnit.SECONDS);
    }

    private void givenCacheLoaderWith(SchemaOptions schemaOptions) {
        config = CouchbaseConfig.builder()
                .withSchemaOptions(schemaOptions)
                .withBucketName("test")
                .withCacheType(Object.class)
                .build();

        loader = new CouchbaseCacheLoader<>(config);
    }


    private Answer<Boolean> getAlternatingBooleanAnswer() {
        return new Answer<Boolean>() {
            boolean returnValue = false;

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                returnValue = !returnValue;

                return returnValue;
            }
        };
    }

}
