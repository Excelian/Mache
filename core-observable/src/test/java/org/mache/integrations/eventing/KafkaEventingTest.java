package org.mache.integrations.eventing;

import org.mache.events.MQFactory;
import org.mache.events.integration.KafkaMQFactory;

import javax.jms.JMSException;
import java.io.IOException;

public class KafkaEventingTest extends TestEventingBase {

    @Override
    protected MQFactory buildMQFactory() throws JMSException, IOException {
        return new KafkaMQFactory("10.28.1.140");
    }
}

