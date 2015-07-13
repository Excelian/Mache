package org.mache.integrations.eventing;

import org.mache.events.MQFactory;
import org.mache.events.integration.RabbitMQFactory;

import javax.jms.JMSException;
import java.io.IOException;

public class RabbitMQEventingTest extends TestEventingBase {

    @Override
    protected MQFactory buildMQFactory() throws JMSException, IOException {
        return new RabbitMQFactory("localhost");
    }
}
