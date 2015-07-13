package org.mache.integrations.eventing;

import org.mache.events.MQFactory;
import org.mache.events.integration.ActiveMQFactory;

import javax.jms.JMSException;

public class ActiveMQEventingTest extends TestEventingBase {

    @Override
    protected MQFactory buildMQFactory() throws JMSException {
        return new ActiveMQFactory("vm://localhost");
    }
}
