package org.mache.integrations.eventing;

import org.junit.Test;
import org.mache.EventType;
import org.mache.coordination.CoordinationEntryEvent;
import org.mache.events.BaseCoordinationEntryEventConsumer;
import org.mache.events.BaseCoordinationEntryEventProducer;
import org.mache.events.MQConfiguration;
import org.mache.events.MQFactory;

import javax.jms.JMSException;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

public abstract class TestEventingBase{

    protected abstract MQFactory buildMQFactory() throws JMSException, IOException;

    class TestEntity{
        public Integer Id;
        public String Name;
    }

    class TestOtherEntity{
        public Integer Id;
        public String Name;
    }

    MQConfiguration getConfigurationForEntity(Class cl)
    {
        MQConfiguration mqConfiguration = new MQConfiguration() {
            @Override
            public String getTopicName() {
                return cl.getName();
            }
        };

        return mqConfiguration;
    }

    @Test
    public void consumerCanReceiveObjectMessagePublishedForSameEvent() throws InterruptedException, JMSException, IOException {

        MQFactory mqFactory = buildMQFactory();
        BaseCoordinationEntryEventConsumer consumer = mqFactory.getConsumer(getConfigurationForEntity(TestEntity.class));
        BaseCoordinationEntryEventProducer producer = mqFactory.getProducer(getConfigurationForEntity(TestEntity.class));

        assertNotNull("Expect consumer to have been created", consumer);
        assertNotNull("Expect producer to have been created", producer);

        CacheEventCollector<Integer> collector = new CacheEventCollector<Integer>();
        consumer.registerEventListener(collector);
        consumer.beginSubscriptionThread();

        CoordinationEntryEvent<String> event = new CoordinationEntryEvent<String>(TestEntity.class.getName(),"ID1",EventType.CREATED, new Date());

        while(collector.pollWithTimeout(250)!=null);//drain queues

        producer.send(event);

        try {
            CoordinationEntryEvent<Integer> receivedEvent = collector.pollWithTimeout(5000);
            assertNotNull("Expected consumer to receive and root an event message", receivedEvent);
            assertEquals(event.getKey(), receivedEvent.getKey());
            assertEquals("Expected Id of message received to same as that sent", event.getUniqueId(),receivedEvent.getUniqueId());
            System.out.println("Test got message");

            assertNull("Expected no more messages", collector.pollWithTimeout(1));
        }

        finally {
            System.out.println("closing");
            producer.close();
            consumer.close();
            mqFactory.close();
        }
    }

    @Test
    public void consumerIgnoresMessagePublishedForDifferentEvent() throws InterruptedException, JMSException, IOException {

        MQFactory mqFactory = buildMQFactory();

        BaseCoordinationEntryEventConsumer consumer = mqFactory.getConsumer(getConfigurationForEntity(TestEntity.class));
        BaseCoordinationEntryEventProducer producer = mqFactory.getProducer(getConfigurationForEntity(TestOtherEntity.class));

        assertNotNull("Expect consumer to have been created", consumer);
        assertNotNull("Expect producer to have been created", producer);

        CacheEventCollector<Integer> collector = new CacheEventCollector<Integer>();
        consumer.registerEventListener(collector);
        consumer.beginSubscriptionThread();

        CoordinationEntryEvent<String> event = new CoordinationEntryEvent<String>(TestOtherEntity.class.getName(),"ID1", EventType.CREATED, new Date());

        while(collector.pollWithTimeout(10)!=null);//drain queues
        producer.send(event);

        try {
            assertNull("Expected no message", collector.pollWithTimeout());
        }

        finally {
            producer.close();
            consumer.close();
            mqFactory.close();
        }
    }

    @Test
    public void multipleConsumersGetACopyOfPublishedEvent() throws InterruptedException, JMSException, IOException {

        MQFactory mqFactory = buildMQFactory();
        BaseCoordinationEntryEventConsumer consumer1 = mqFactory.getConsumer(getConfigurationForEntity(TestEntity.class));
        BaseCoordinationEntryEventConsumer consumer2 = mqFactory.getConsumer(getConfigurationForEntity(TestEntity.class));
        BaseCoordinationEntryEventProducer producer = mqFactory.getProducer(getConfigurationForEntity(TestEntity.class));

        CacheEventCollector<Integer> collector1 = new CacheEventCollector<Integer>();
        CacheEventCollector<Integer> collector2 = new CacheEventCollector<Integer>();

        consumer1.registerEventListener(collector1);
        consumer1.beginSubscriptionThread();
        while(collector1.pollWithTimeout(10)!=null);//drain queues

        consumer2.registerEventListener(collector2);
        consumer2.beginSubscriptionThread();
        while(collector2.pollWithTimeout(10)!=null);//drain queues

        CoordinationEntryEvent<String> event = new CoordinationEntryEvent<String>(TestEntity.class.getName(), "ID1", EventType.CREATED, new Date());

        //Publish just the once
        producer.send(event);


        try {
            CoordinationEntryEvent<Integer> receivedEvent;

            receivedEvent = collector1.pollWithTimeout();
            assertNotNull("Expected FIRST consumer to receive and root an event message", receivedEvent);
            assertEquals(receivedEvent.getUniqueId(), event.getUniqueId());
            assertEquals(receivedEvent.getKey(), event.getKey());
            assertNull("Expected no more messages", collector1.pollWithTimeout(1));

            receivedEvent = collector2.pollWithTimeout();
            assertNotNull("Expected SECOND consumer to receive and root an event message", receivedEvent);
            assertEquals(receivedEvent.getUniqueId(), event.getUniqueId());
            assertEquals(receivedEvent.getKey(), event.getKey());
            assertNull("Expected no more messages", collector2.pollWithTimeout(1));
        }
        finally {
            producer.close();
            consumer1.close();
            consumer2.close();
            mqFactory.close();
        }


    }
}
