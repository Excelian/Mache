package com.excelian.mache.jmeter.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.JMSException;

import com.excelian.mache.core.Mache;
import com.excelian.mache.core.MacheFactory;
import com.excelian.mache.core.SchemaOptions;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.integration.ActiveMQFactory;
import com.excelian.mache.events.integration.DefaultActiveMqConfig;
import com.excelian.mache.mongo.MongoDBCacheLoader;
import com.excelian.mache.observable.MessageQueueObservableCacheFactory;
import com.excelian.mache.observable.ObservableCacheFactory;
import com.excelian.mache.observable.utils.UUIDUtils;
import com.fasterxml.uuid.Generators;
import com.mongodb.Mongo;
import com.mongodb.ServerAddress;

public class MongoConsoleMultiThreadedTester {
	private final ExecutorService writerExecutor;
	private final ExecutorService readerExecutor;
	private final int numerOfWriters;
	private final int numberOfReaders;

	public MongoConsoleMultiThreadedTester(int numerOfWriters,
			int numberOfReaders) {
		this.numerOfWriters = numerOfWriters;
		this.numberOfReaders = numberOfReaders;
		writerExecutor = Executors.newFixedThreadPool(numerOfWriters);
		readerExecutor = Executors.newFixedThreadPool(numberOfReaders);
	}

	public void run(int writerSleepMs, int readerSleepMs, int readerTimeoutMs,
			String entityKey, String mongoIP, int mongoPort, String activeMq, String keySpace)
			throws InterruptedException, ExecutionException, JMSException {
		final List<Future<Statistics>> results = new ArrayList<Future<Statistics>>(
				numerOfWriters + numberOfReaders);

		for (int i = 0; i < numerOfWriters; ++i) {
			results.add(writerExecutor.submit(new WriterCallable(writerSleepMs,
					entityKey, mongoIP, mongoPort, activeMq, keySpace)));
		}
		for (int i = 0; i < numberOfReaders; ++i) {
			results.add(readerExecutor.submit(new ReaderCallable(readerSleepMs,
					readerTimeoutMs, entityKey, mongoIP, mongoPort, activeMq, keySpace)));
		}

		readerExecutor.shutdown();
		readerExecutor.awaitTermination(20, TimeUnit.MINUTES);

		// calculate stats
		final Map<ThreadType, Statistics> aggregatedStats = new HashMap<ThreadType, Statistics>();
		for (final Future<Statistics> stat : results) {
			final Statistics statistics = stat.get();
			if (!aggregatedStats.containsKey(statistics.threadType)) {
				aggregatedStats.put(statistics.threadType, statistics);
			} else {
				Statistics temp = aggregatedStats.get(statistics.threadType);
				temp.okCount += statistics.okCount;
				temp.errorsCount += statistics.errorsCount;

				if (statistics.milisStarted < temp.milisStarted) {
					temp.milisStarted = statistics.milisStarted;
				}

				if (statistics.milisEnded > temp.milisEnded) {
					temp.milisEnded = statistics.milisEnded;
				}

				temp.runsCount += statistics.runsCount;
			}
		}

		// print stats
		final Statistics readersStats = aggregatedStats.get(ThreadType.Reader);
		System.out
				.println("No readers: "
						+ numberOfReaders
						+ " throughput "
						+ (readersStats.runsCount / (double) (readersStats.milisEnded - readersStats.milisStarted)));
	}

	public static void main(String[] args) throws InterruptedException,
			ExecutionException, JMSException {
		final int numerOfWriters = 1;
		final int writerSleepMs = 100;
		final int numberOfReaders = 50;
		final int readerSleepMs = 0;
		final int readerTimeoutMs = 50;
		final String entityKey = "consleKey1234";
		final String mongoIP = "10.28.1.139";
		final int mongoPort = 27017;
		final String activeMq = "vm://localhost";
		final String keySpace = "ConsolReadThrought";

		new MongoConsoleMultiThreadedTester(numerOfWriters, numberOfReaders)
				.run(writerSleepMs, readerSleepMs, readerTimeoutMs, entityKey,
						mongoIP, mongoPort, activeMq, keySpace);

		return;
	}

	static enum ThreadType {
		Reader, Writer;
	}

	static class Statistics {
		volatile ThreadType threadType;

		volatile int runsCount;
		volatile long milisStarted;
		volatile long milisEnded;

		volatile int okCount;
		volatile int errorsCount;
	}

	static abstract class MongoBaseCallable {
		protected final String mongoIP;
		protected final int mongoPort;
		protected final String activeMq;

		protected final String mongoKeyspace;

		private final ActiveMQFactory<String> mqFactory1;
		protected final Mache<String, MongoTestEntity> cache1;

		protected MongoBaseCallable(final String mongoIP, final int mongoPort,
				final String activeMq, final String mongoKeyspace)
				throws JMSException {
			this.mongoIP = mongoIP;
			this.mongoPort = mongoPort;
			this.activeMq = activeMq;
			this.mongoKeyspace = mongoKeyspace;

			mqFactory1 = new ActiveMQFactory<String>(activeMq,
					new DefaultActiveMqConfig());
			ObservableCacheFactory<String, MongoTestEntity, Mongo> cacheFactory1 = new MessageQueueObservableCacheFactory<String, MongoTestEntity, Mongo>(
					mqFactory1, getMQConfiguration(),
					new MacheFactory<String, MongoTestEntity, Mongo>(),
					new UUIDUtils());
			cache1 = cacheFactory1.createCache(new MongoDBCacheLoader<>(
					MongoTestEntity.class, new CopyOnWriteArrayList<>(Arrays
							.asList(new ServerAddress(mongoIP, mongoPort))),
					SchemaOptions.CREATEANDDROPSCHEMA, mongoKeyspace));
		}

		private MQConfiguration getMQConfiguration() {
			return new MQConfiguration() {
				@Override
				public String getTopicName() {
					return "testTopic";
				}
			};
		}
		

	    public void teardown() {
	        if (cache1 != null) cache1.close();
	        if (mqFactory1 != null) mqFactory1.close();
	    }
	}

	static class WriterCallable extends MongoBaseCallable implements
			Callable<Statistics> {
		private static AtomicReference<String> lastWrittenValue = new AtomicReference<String>(
				null);
		private final int writerSleepMs;
		private final String entityKey;

		public WriterCallable(int writerSleepMs, String entityKey,
				final String mongoIP, final int mongoPort,
				final String activeMq, final String mongoKeyspace)
				throws JMSException {
			super(mongoIP, mongoPort, activeMq, mongoKeyspace);
			this.writerSleepMs = writerSleepMs;
			this.entityKey = entityKey;

		}

		public static String getLastWrittenValue() {
			return lastWrittenValue.get();
		}

		@Override
		public Statistics call() throws InterruptedException {
			final Statistics result = new Statistics();
			result.milisStarted = System.currentTimeMillis();

			for (int i = 0; i < 20000000; ++i) {
				if (!Thread.interrupted()) {
					String nValue = generateValue();
					MongoTestEntity tE = new MongoTestEntity(this.entityKey,
							nValue);
					cache1.put(this.entityKey, tE);
					lastWrittenValue.set(nValue);
					result.runsCount++;
					result.okCount++;
					Thread.sleep(writerSleepMs);
				}
			}

			result.milisEnded = System.currentTimeMillis();
			
			teardown();
			return result;
		}

		private String generateValue() {
			return Generators.timeBasedGenerator().generate().toString();
		}
	}

	static class ReaderCallable extends MongoBaseCallable implements Callable<Statistics> {
		private final int readerSleepMs;
		private final int readerTimeoutMs;
		private final String entityKey;

		public ReaderCallable(int readerSleepMs, int readerTimeoutMs,
				String entityKey, final String mongoIP, final int mongoPort,
				final String activeMq, final String mongoKeyspace)
				throws JMSException {
			super(mongoIP, mongoPort, activeMq, mongoKeyspace);
			this.readerSleepMs = readerSleepMs;
			this.readerTimeoutMs = readerTimeoutMs;
			this.entityKey = entityKey;
		}


		@Override
		public Statistics call() throws InterruptedException {
			while(WriterCallable.getLastWrittenValue() == null) {
				Thread.sleep(0);
			}
			
			final Statistics result = new Statistics();
			result.milisStarted = System.currentTimeMillis();

			for (int i = 0; i < 500000; ++i) {
				if (!Thread.interrupted()) {
					long started = System.currentTimeMillis();
					String nValue = null;
					do {
						nValue = cache1.get(this.entityKey).description;
						if (WriterCallable.getLastWrittenValue().equals(nValue)) {
							break;
						}
						Thread.sleep(readerSleepMs);
					} while (!WriterCallable.getLastWrittenValue().equals(nValue) || System.currentTimeMillis() - started < readerTimeoutMs);
					result.runsCount++;
					if (WriterCallable.getLastWrittenValue().equals(nValue))
						result.okCount++;
					else
						result.errorsCount++;
				}
			}

			result.milisEnded = System.currentTimeMillis();

			teardown();
			return result;
		}

	}
}
