package com.excelian.mache.caffeine;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class CaffeineMacheProvisionerShould {

    @Test
    public void createProvisionerWithDefaults() throws Throwable {
        String config = CaffeineMacheProvisioner.caffeine().toString();
        assertThat(config, containsString("maximumSize=10000"));
        assertThat(config, containsString("expireAfterWrite=" + TimeUnit.DAYS.toNanos(1)));
        assertThat(config, containsString("expireAfterAccess=" + TimeUnit.DAYS.toNanos(1)));
        assertThat(config, containsString("valueStrength=soft"));
    }

}
