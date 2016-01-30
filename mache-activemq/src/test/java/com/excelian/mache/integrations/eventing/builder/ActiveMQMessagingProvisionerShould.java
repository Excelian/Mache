package com.excelian.mache.integrations.eventing.builder;

import com.excelian.mache.core.HashMapCacheLoader;
import com.excelian.mache.core.HashMapMache;
import com.excelian.mache.core.Mache;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;

import static com.excelian.mache.builder.MacheBuilder.mache;
import static com.excelian.mache.events.integration.builder.ActiveMQMessagingProvisioner.activemq;
import static org.junit.Assert.assertNotNull;

public class ActiveMQMessagingProvisionerShould {

    @Test
    public void createProvisioner() throws Throwable {
        Mache<String, String> test = mache(String.class, String.class)
                .cachedBy((k, v, cacheLoader) -> new HashMapMache<>(cacheLoader))
                .storedIn(((k, v) -> new HashMapCacheLoader<>(v)))
                .withMessaging(activemq()
                        .withTopic("test")
                        .withConnectionFactory(new ActiveMQConnectionFactory("vm://localhost"))
                        .build())
                .macheUp();

        assertNotNull(test);
    }
}
