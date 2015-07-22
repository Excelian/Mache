package org.mache.integrations.eventing;

import java.io.IOException;

import javax.jms.JMSException;

import org.mache.events.MQFactory;
import org.mache.events.integration.RabbitMQFactory;

public class RabbitMQEventingTest extends TestEventingBase {

    @Override
    protected MQFactory buildMQFactory() throws JMSException, IOException {
        return new RabbitMQFactory("localhost");
    }
}
