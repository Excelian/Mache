package com.excelian.mache.integrations.eventing;

import com.excelian.mache.events.BaseCoordinationEntryEventConsumer;
import com.excelian.mache.events.BaseCoordinationEntryEventProducer;
import com.excelian.mache.events.MQConfiguration;
import com.excelian.mache.events.MQFactory;
import com.excelian.mache.observable.coordination.CoordinationEntryEvent;
import com.excelian.mache.observable.utils.UuidUtils;
import com.fasterxml.uuid.Generators;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.excelian.mache.observable.EventType.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public abstract class TestEventingBase {
    @Rule
    public Timeout globalTimeout = new Timeout(20, TimeUnit.SECONDS);

    private static final Logger LOG = LoggerFactory.getLogger(TestEventingBase.class);
    protected MQFactory<String> theMqFactory;
    protected BaseCoordinationEntryEventProducer<String> theProducer;
    protected BaseCoordinationEntryEventConsumer<String> theConsumer;

    private CacheEventCollector<String> theSpiedEventCollector;

    protected abstract MQFactory<String> buildMQFactory() throws JMSException, IOException;

    MQConfiguration getConfigurationForEntity(final Class cl) {
        return cl::getName;
    }

    /**
     * May be overridden for messaging systems requiring a longer living connection.
     *
     * @throws IOException Exception.
     */
    @After
    public void afterEachTestcase() throws IOException {
        if (theProducer != null) {
            theProducer.close();
        }
        if (theMqFactory != null) {
            theMqFactory.close();
        }
        if (theConsumer != null) {
            theConsumer.close();
        }
    }

    @Test
    public void consumerCanReceiveObjectMessagePublishedForSameEvent() throws Exception {
        given_anMQFactory();
        BaseCoordinationEntryEventConsumer<String> consumer =
            theMqFactory.getConsumer(getConfigurationForEntity(TestEntity.class));
        BaseCoordinationEntryEventProducer<String> producer =
            theMqFactory.getProducer(getConfigurationForEntity(TestEntity.class));

        assertNotNull("Expect consumer to have been created", consumer);
        assertNotNull("Expect producer to have been created", producer);

        CacheEventCollector<String> collector = new CacheEventCollector<>();
        consumer.registerEventListener(collector);
        consumer.beginSubscriptionThread();

        CoordinationEntryEvent<String> event = new CoordinationEntryEvent<>(getUuid(), TestEntity.class.getName(),
            "ID1", CREATED, new UuidUtils());

        drainQueues(collector, 150);
        producer.send(event);

        try {
            CoordinationEntryEvent<String> receivedEvent = collector.take();
            assertNotNull("Expected consumer to receive and route an event message but got none", receivedEvent);
            assertEquals(event.getKey(), receivedEvent.getKey());
            assertEquals("Expected id of message received to same as that sent", event.getUniqueId(),
                receivedEvent.getUniqueId());
            LOG.info("Test got message");

            assertNull("Expected no more messages", collector.pollWithTimeout(150));
        } finally {
            producer.close();
            consumer.close();
        }
    }

    private void drainQueues(CacheEventCollector collector, int millisTimeout) throws InterruptedException {
        while (collector.pollWithTimeout(millisTimeout) != null) {
            // drain queues
        }
    }

    protected UUID getUuid() {
        return Generators.randomBasedGenerator().generate();
    }

    @Test
    public void consumerIgnoresMessagePublishedForDifferentEntity() throws Exception {
        given_anMQFactory();

        BaseCoordinationEntryEventProducer<String> producer =
            theMqFactory.getProducer(getConfigurationForEntity(TestOtherEntity.class));
        BaseCoordinationEntryEventConsumer<String> consumer =
            theMqFactory.getConsumer(getConfigurationForEntity(TestEntity.class));

        assertNotNull("Expect consumer to have been created", consumer);
        assertNotNull("Expect producer to have been created", producer);

        CacheEventCollector<String> collector = new CacheEventCollector<>();
        consumer.registerEventListener(collector);
        consumer.beginSubscriptionThread();

        CoordinationEntryEvent<String> event = new CoordinationEntryEvent<>(getUuid(),
            TestOtherEntity.class.getName(), "ID1", CREATED, new UuidUtils());

        producer.send(event);
        assertNull("Expected no message", collector.pollWithTimeout(200));
    }

    @Test
    public void multipleConsumersGetACopyOfPublishedEvent() throws InterruptedException, JMSException, IOException {
        given_anMQFactory();

        BaseCoordinationEntryEventConsumer<String> consumer1 =
            theMqFactory.getConsumer(getConfigurationForEntity(TestEntity.class));

        CacheEventCollector<String> collector1 = new CacheEventCollector<>();
        consumer1.registerEventListener(collector1);
        consumer1.beginSubscriptionThread();
        drainQueues(collector1, 120);

        BaseCoordinationEntryEventConsumer<String> consumer2 =
            theMqFactory.getConsumer(getConfigurationForEntity(TestEntity.class));
        CacheEventCollector<String> collector2 = new CacheEventCollector<>();
        consumer2.registerEventListener(collector2);
        consumer2.beginSubscriptionThread();
        drainQueues(collector2, 120);

        CoordinationEntryEvent<String> event = new CoordinationEntryEvent<>(getUuid(), TestEntity.class.getName(),
            "ID1", CREATED, new UuidUtils());

        BaseCoordinationEntryEventProducer<String> producer =
            theMqFactory.getProducer(getConfigurationForEntity(TestEntity.class));
        producer.send(event);

        try {
            CoordinationEntryEvent<String> receivedEvent;

            receivedEvent = collector1.take();
            assertNotNull("Expected FIRST consumer to receive and route an event message", receivedEvent);
            assertEquals(receivedEvent.getUniqueId(), event.getUniqueId());
            assertEquals(receivedEvent.getKey(), event.getKey());
            assertNull("Expected no more messages", collector1.pollWithTimeout(150));

            receivedEvent = collector2.take();
            assertNotNull("Expected SECOND consumer to receive and route an event message", receivedEvent);
            assertEquals(receivedEvent.getUniqueId(), event.getUniqueId());
            assertEquals(receivedEvent.getKey(), event.getKey());
            assertNull("Expected no more messages", collector2.pollWithTimeout(150));
        } finally {
            producer.close();
            consumer1.close();
            consumer2.close();
        }
    }

    @Test
    public void testCreatedEventsArePropagatedToListeners() throws Exception {
        given_anMQFactory();
        given_anEventProducer();
        given_anEventCollector();
        when_aCreatedEventIsRaisedBy(theProducer);
        then_aCreatedEventIsReceivedBy(theSpiedEventCollector);
    }

    @Test
    public void testUpdatedEventsArePropagatedToListeners() throws Exception {
        given_anMQFactory();
        given_anEventProducer();
        given_anEventCollector();
        when_anUpdatedEventIsRaisedBy(theProducer);
        then_anUpdatedEventIsReceivedBy(theSpiedEventCollector);
    }

    @Test
    public void testRemovedEventsArePropagatedToListeners() throws Exception {
        given_anMQFactory();
        given_anEventProducer();
        given_anEventCollector();
        when_aRemovedEventIsRaisedBy(theProducer);
        then_aRemovedEventIsReceivedBy(theSpiedEventCollector);
    }

    @Test
    public void testInvalidateEventsArePropagatedToListeners() throws Exception {
        given_anMQFactory();
        given_anEventProducer();
        given_anEventCollector();
        when_anInvalidatedEventIsRaisedBy(theProducer);
        then_anInvalidatedEventIsReceivedBy(theSpiedEventCollector);
    }

    private void given_anEventCollector() throws IOException, JMSException, InterruptedException {
        theConsumer = theMqFactory.getConsumer(getConfigurationForEntity(TestEntity.class));
        final CacheEventCollector<String> eventCollector = new CacheEventCollector<>();
        theSpiedEventCollector = spy(eventCollector);
        theConsumer.registerEventListener(theSpiedEventCollector);
        theConsumer.beginSubscriptionThread();
        drainQueues(eventCollector, 120);
    }

    private void given_anEventProducer() {
        theProducer = theMqFactory.getProducer(getConfigurationForEntity(TestEntity.class));
    }

    private void given_anMQFactory() throws JMSException, IOException {
        theMqFactory = buildMQFactory();
    }

    private void when_aCreatedEventIsRaisedBy(BaseCoordinationEntryEventProducer<String> producer) {
        producer.send(new CoordinationEntryEvent<>(getUuid(), TestEntity.class.getName(),
            "ID1", CREATED, new UuidUtils()));
    }

    private void when_anInvalidatedEventIsRaisedBy(BaseCoordinationEntryEventProducer<String> producer) {
        producer.send(new CoordinationEntryEvent<>(getUuid(), TestEntity.class.getName(),
            "ID1", INVALIDATE, new UuidUtils()));
    }

    private void when_aRemovedEventIsRaisedBy(BaseCoordinationEntryEventProducer<String> producer) {
        producer.send(new CoordinationEntryEvent<>(getUuid(), TestEntity.class.getName(),
            "ID1", REMOVED, new UuidUtils()));
    }

    private void when_anUpdatedEventIsRaisedBy(BaseCoordinationEntryEventProducer<String> producer) {
        producer.send(new CoordinationEntryEvent<>(getUuid(), TestEntity.class.getName(),
            "ID1", UPDATED, new UuidUtils()));
    }

    @SuppressWarnings("unchecked")
    private void then_aCreatedEventIsReceivedBy(CacheEventCollector<String> consumer) throws InterruptedException {
        consumer.take();
        verify(consumer).onCreated(any(Iterable.class));
    }

    @SuppressWarnings("unchecked")
    private void then_anUpdatedEventIsReceivedBy(CacheEventCollector<String> consumer) throws InterruptedException {
        consumer.take();
        verify(consumer).onUpdated(any(Iterable.class));
    }

    @SuppressWarnings("unchecked")
    private void then_aRemovedEventIsReceivedBy(CacheEventCollector<String> consumer) throws InterruptedException {
        consumer.take();
        verify(consumer).onRemoved(any(Iterable.class));
    }

    @SuppressWarnings("unchecked")
    private void then_anInvalidatedEventIsReceivedBy(CacheEventCollector<String> consumer) throws InterruptedException {
        consumer.take();
        verify(consumer).onInvalidate(any(Iterable.class));
    }

    class TestEntity {
        public Integer id;
        public String name;
    }

    class TestOtherEntity {
        public Integer id;
        public String name;
    }
}
