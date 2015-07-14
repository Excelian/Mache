package org.mache;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import javax.jms.JMSException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;
import org.mache.events.integration.ActiveMQFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CacheFactoryImplIntegrationTest {
	private static final String LOCAL_MQ = "vm://localhost";

	ExCacheLoader<String, String, String> cacheLoader;
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
	
	ExCache<String, String> spiedCache1;

	String testKey = "testKey";
	String testValue = "testValue";
	String testValue2 = "testValue2";

	@Mock
	CacheThingFactory spiedCacheThingFactory;
	
	CacheThingFactory cacheThingFactory;

	@Before
	public void beforeTest() throws JMSException {
		MockitoAnnotations.initMocks(this);
		
		cacheLoader = new InMemoryCacheLoader("loaderForTestEntity");

		cacheThingFactory = new CacheThingFactory();

		mqFactory1 = new ActiveMQFactory(LOCAL_MQ);
		cacheFactory1 = new CacheFactoryImpl(mqFactory1, mqConfiguration, spiedCacheThingFactory);

		spiedCache1 = spy(cacheThingFactory.create(cacheLoader, (String[]) null));
		when(spiedCacheThingFactory.create(cacheLoader, (String[]) null)).thenReturn(spiedCache1);

		mqFactory2 = new ActiveMQFactory(LOCAL_MQ);
		cacheFactory2 = new CacheFactoryImpl(mqFactory2, mqConfiguration, cacheThingFactory);
	}

	@After
	public void TearDown() throws IOException {
		mqFactory1.close();
		mqFactory2.close();
	}

	@Test
	public void shouldProperlySetupCachesUsingSameCacheLoader() throws ExecutionException {
		ExCache<String, String> cache1 = cacheFactory1.createCache(cacheLoader);
		cache1.put(testKey, testValue);

		ExCache<String, String> cache2 = cacheFactory2.createCache(cacheLoader);
		assertEquals(testValue, cache2.get(testKey));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void shouldProperlyInvalidateFromAnotherCache() throws ExecutionException, InterruptedException {
		ExCache<String, String> cache1 = cacheFactory1.createCache(cacheLoader);
		ExCache<String, String> cache2 = cacheFactory2.createCache(cacheLoader);

		reset(spiedCache1);
		cache2.put(testKey, testValue2);
		Thread.sleep(5000);
		verify(spiedCache1).remove(testKey);
	}
}
