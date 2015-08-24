package com.excelian.mache.integrations.eventing;

import com.excelian.mache.events.integration.DefaultRabbitMqConfig;
import com.excelian.mache.events.integration.RabbitMQFactory;
import org.junit.Assume;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;

/**
 * Borrowed idea from org.springframework.amqp.rabbit.test.BrokerRunning
 */
public class RabbitMQBrokerRunning extends TestWatcher {

    private static boolean brokerOnline = true;

    public static RabbitMQBrokerRunning isRunning() {
        return new RabbitMQBrokerRunning();
    }

    @Override
    public Statement apply(Statement base, Description description) {

        Assume.assumeTrue("Could not connect to Rabbit MQ", brokerOnline);

        RabbitMQFactory factory = null;
        try {
            factory = new RabbitMQFactory(null, new DefaultRabbitMqConfig());
        } catch (Exception e) {
            brokerOnline = false;
            Assume.assumeNoException("Could not connect to Rabbit MQ", e);
        } finally {
            if (factory != null) {
                try {
                    factory.close();
                } catch (IOException e) {
                }
            }
        }

        return super.apply(base, description);
    }
}