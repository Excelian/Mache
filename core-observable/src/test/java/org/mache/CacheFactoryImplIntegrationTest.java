package org.mache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.jms.JMSException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;
import org.mache.events.integration.ActiveMQFactory;
import org.mache.utils.UUIDUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CacheFactoryImplIntegrationTest {
	private static final String LOCAL_MQ = "vm://localhost";

	ExCacheLoader<String, TestEntity, String> cacheLoader;
	MQConfiguration mqConfiguration = new MQConfiguration() {
		@Override
		public String getTopicName() {
			return "testTopic";
		}
	};

	MQFactory mqFactory1;
	CacheFactory cacheFactory1;

	MQFactory mqFactory2;
	CacheFactory cacheFactory2;
	
	ExCache<String, TestEntity> spiedCache1;

	TestEntity testValue = new TestEntity("testValue");
	TestEntity testValue2 = new TestEntity("testValue2");

	@Mock
	CacheThingFactory spiedCacheThingFactory;
	
	CacheThingFactory cacheThingFactory;

	private final UUIDUtils uuidUtils = new UUIDUtils();

	@Before
	public void beforeTest() throws JMSException {
		MockitoAnnotations.initMocks(this);
		
		cacheLoader = new InMemoryCacheLoader("loaderForTestEntity");

		cacheThingFactory = new CacheThingFactory();

		mqFactory1 = new ActiveMQFactory(LOCAL_MQ);
		cacheFactory1 = new CacheFactoryImpl(mqFactory1, mqConfiguration, spiedCacheThingFactory, uuidUtils);

		spiedCache1 = spy(cacheThingFactory.create(cacheLoader, (String[]) null));
		when(spiedCacheThingFactory.create(cacheLoader, (String[]) null)).thenReturn(spiedCache1);

		mqFactory2 = new ActiveMQFactory(LOCAL_MQ);
		cacheFactory2 = new CacheFactoryImpl(mqFactory2, mqConfiguration, cacheThingFactory, uuidUtils);
	}

	@After
	public void TearDown() throws IOException {
		mqFactory1.close();
		mqFactory2.close();
	}

	@Test
	public void shouldProperlySetupCachesUsingSameCacheLoader() throws ExecutionException, InterruptedException {
		ExCache<String, TestEntity> cache1 = cacheFactory1.createCache(cacheLoader);
		cache1.put(testValue.pkey, testValue);

		ExCache<String, TestEntity> cache2 = cacheFactory2.createCache(cacheLoader);

		assertEquals(testValue, cache2.get(testValue.pkey));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldProperlyInvalidateFromAnotherCacheWhenItemPut() throws ExecutionException, InterruptedException {
		ExCache<String, TestEntity> cache1 = cacheFactory1.createCache(cacheLoader);
		ExCache<String, TestEntity> cache2 = cacheFactory2.createCache(cacheLoader);

		reset(spiedCache1);
		cache2.put(testValue2.pkey, testValue2);

		Thread.sleep(2000);//give time for the message to propagate and invalidate to be called

		verify(spiedCache1).invalidate(testValue2.pkey);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldNotInvalidateFromAnotherCacheWhenItemFetched() throws ExecutionException, InterruptedException {
		ExCache<String, TestEntity> cache1 = cacheFactory1.createCache(cacheLoader);
		ExCache<String, TestEntity> cache2 = cacheFactory2.createCache(cacheLoader);

		/* insert data into loader and ensure it is within cache */
		cache1.put(testValue2.pkey, testValue2);
		assertNotNull(cache1.get(testValue2.pkey));

		Thread.sleep(1000);//give time for the message to propagate and invalidate to be called from put
		verify(spiedCache1).invalidate(testValue2.pkey);

		/* reset mocks */
		reset(spiedCache1);

		/* pull it into 2nd cache (this should NOT affect any other cache*/
		assertNotNull(cache2.get(testValue2.pkey));
		Thread.sleep(1000);//give time for any messages to propagate and invalidate to 'potentially' called

		verify(spiedCache1, never()).invalidate(testValue2.pkey);
	}
}
