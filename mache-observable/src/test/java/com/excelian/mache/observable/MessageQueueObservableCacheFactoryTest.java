package com.excelian.mache.observable;

import com.excelian.mache.core.AbstractCacheLoader;
import com.excelian.mache.core.InMemoryCacheLoader;
import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.TestEntity;
import com.excelian.mache.core.TestEntity2;
import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.excelian.mache.observable.utils.UUIDUtils;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MessageQueueObservableCacheFactoryTest {

    private final UUIDUtils uuidUtils = new UUIDUtils();
    private final Gson gson = new Gson();
    AbstractCacheLoader<String, TestEntity, Object> cacheLoader1;
    AbstractCacheLoader<String, TestEntity, Object> cacheLoader2;
    MQConfiguration mqConfiguration = () -> "testTopic";
    @Mock
    MQFactory<String> mqFactory1;
    @Mock
    MQFactory<String> mqFactory2;
    ObservableCacheFactory<String, TestEntity> observableCacheFactory1;
    ObservableCacheFactory<String, TestEntity> observableCacheFactory2;
    Mache<String, TestEntity> unspiedCache1;
    Mache<String, TestEntity> spiedCache1;
    TestEntity testValue = new TestEntity("testValue");
    TestEntity testValue2 = new TestEntity("testValue2");
    MacheFactory macheFactory = new MacheFactory();
    String currentMessage = null;

    @Before
    public void beforeTest() throws Throwable {
        cacheLoader1 = new InMemoryCacheLoader<>(TestEntity.class);
        cacheLoader2 = new InMemoryCacheLoader<>(TestEntity.class);

        BaseCoordinationEntryEventConsumer<String> inMemoryConsumer1 = getInMemoryConsumer();
        BaseCoordinationEntryEventConsumer<String> inMemoryConsumer2 = getInMemoryConsumer();

        when(mqFactory1.getConsumer(mqConfiguration)).thenReturn(inMemoryConsumer1);
        when(mqFactory1.getProducer(mqConfiguration)).thenReturn(getInMemProducer(inMemoryConsumer1, inMemoryConsumer2));

        when(mqFactory2.getConsumer(mqConfiguration)).thenReturn(inMemoryConsumer2);
        when(mqFactory2.getProducer(mqConfiguration)).thenReturn(getInMemProducer(inMemoryConsumer1, inMemoryConsumer2));

        observableCacheFactory1 = new MessageQueueObservableCacheFactory<>(mqFactory1, mqConfiguration, uuidUtils);
        observableCacheFactory2 = new MessageQueueObservableCacheFactory<>(mqFactory2, mqConfiguration, uuidUtils);

        Mache<String, TestEntity> macheImpl = macheFactory.create(cacheLoader1);
        spiedCache1 = spy(macheImpl);
        unspiedCache1 = observableCacheFactory1.createCache(spiedCache1);
    }

    @After
    public void tearDown() throws IOException {
        mqFactory1.close();
        mqFactory2.close();
    }

    @Test
    public void shouldProperlySetupCachesUsingSameCacheLoader() throws ExecutionException, InterruptedException {
        Mache<String, TestEntity> cache1 = observableCacheFactory1.createCache(macheFactory.create(cacheLoader1));
        cache1.put(testValue.pkey, testValue);

        Mache<String, TestEntity> cache2 = observableCacheFactory2.createCache(macheFactory.create(cacheLoader1));
        assertEquals(testValue, cache2.get(testValue.pkey));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldProperlyInvalidateFromAnotherCacheWhenItemPut() throws ExecutionException, InterruptedException {
        Mache<String, TestEntity> cache2 = observableCacheFactory2.createCache(macheFactory.create(cacheLoader2));
        reset(spiedCache1);
        cache2.put(testValue2.pkey, testValue2);
        verify(spiedCache1).invalidate(testValue2.pkey);
    }

    @Test
    public void shouldProperlyPropagateValues() throws ExecutionException, InterruptedException, JMSException {
        AbstractCacheLoader<String, TestEntity2, Object> cacheLoader =
                new InMemoryCacheLoader<>(TestEntity2.class);
        ObservableCacheFactory<String, TestEntity2> observableCacheFactory1 =
                new MessageQueueObservableCacheFactory<>(mqFactory1, mqConfiguration, new UUIDUtils());
        ObservableCacheFactory<String, TestEntity2> observableCacheFactory2 =
                new MessageQueueObservableCacheFactory<>(mqFactory2, mqConfiguration, new UUIDUtils());

        Mache<String, TestEntity2> cache1 = observableCacheFactory1.createCache(macheFactory.create(cacheLoader));
        Mache<String, TestEntity2> cache2 = observableCacheFactory2.createCache(macheFactory.create(cacheLoader));

        final String key1 = "X1";
        final String val1 = "someValue1";

        cache1.put(key1, new TestEntity2(key1, val1));
        assertEquals(val1, cache2.get(key1).otherValue);

        final String val2 = "someValue2";
        cache1.put(key1, new TestEntity2(key1, val2));
        assertEquals(val2, cache2.get(key1).otherValue);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotInvalidateFromAnotherCacheWhenItemFetched() throws ExecutionException, InterruptedException {
        // a get in one cache does nothing to the other

        Mache<String, TestEntity> cache1 = observableCacheFactory1.createCache(macheFactory.create(cacheLoader1));
        Mache<String, TestEntity> cache2 = observableCacheFactory2.createCache(macheFactory.create(cacheLoader1));

		/* insert data into loader and ensure it is within cache */
        cache1.put(testValue2.pkey, testValue2);
        assertNotNull(cache1.get(testValue2.pkey));

        /* reset mocks */
        reset(spiedCache1);
        /* pull it into 2nd cache (this should NOT affect any other cache*/
        assertNotNull(cache2.get(testValue2.pkey));

        verify(spiedCache1, never()).invalidate(testValue2.pkey);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotInvalidateSameCacheOnPut() throws ExecutionException, InterruptedException {
        reset(spiedCache1);
        unspiedCache1.put(testValue2.pkey, testValue2);

        verify(spiedCache1, times(0)).invalidate(testValue2.pkey);
    }

    private BaseCoordinationEntryEventConsumer<String> getInMemoryConsumer() {
        return new BaseCoordinationEntryEventConsumer<String>("testTopic") {
            @Override
            public void beginSubscriptionThread() throws InterruptedException, JMSException, IOException {
                final CoordinationEntryEvent<String> event = gson.fromJson(currentMessage,
                        new TypeToken<CoordinationEntryEvent<String>>() {
                        }.getType());

                if (event != null) {
                    routeEventToListeners(event);
                }
            }

            @Override
            public void close() {
                // NOOP
            }
        };
    }

    @SafeVarargs
    private final BaseCoordinationEntryEventProducer<String> getInMemProducer(
            BaseCoordinationEntryEventConsumer<String>... consumerList) {

        return new BaseCoordinationEntryEventProducer<String>("testTopic") {
            BaseCoordinationEntryEventConsumer<String>[] consumers = consumerList;

            @Override
            public void send(CoordinationEntryEvent<String> event) {
                currentMessage = gson.toJson(event);
                for (BaseCoordinationEntryEventConsumer<String> consumer : consumers) {
                    try {
                        consumer.beginSubscriptionThread();
                    } catch (Exception e) {
                        // NOOP
                    }
                }
            }

            @Override
            public void close() {
                // NOOP
            }

            public void dispatch() {

            }
        };
    }
}
