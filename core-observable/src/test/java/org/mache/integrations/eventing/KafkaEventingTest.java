package org.mache.integrations.eventing;

import com.codeaffine.test.ConditionalIgnoreRule;
import org.junit.Rule;
import org.mache.NoRunningKafkaForTests;
import org.mache.NotRunningInExcelian;
import org.mache.events.MQFactory;
import org.mache.events.integration.KafkaMQFactory;

import javax.jms.JMSException;
import java.io.IOException;

import static com.codeaffine.test.ConditionalIgnoreRule.*;

@IgnoreIf(condition = NoRunningKafkaForTests.class)
public class KafkaEventingTest extends TestEventingBase {

    @Rule
    public final ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

    @Override
    protected MQFactory buildMQFactory() throws JMSException, IOException {
        return new KafkaMQFactory(new NoRunningKafkaForTests().HostName());
    }
}

