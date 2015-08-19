package org.mache.utils;

import com.fasterxml.uuid.Generators;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class UUIDUtilsTest {
    private final UUIDUtils uuidUtils = new UUIDUtils();

    private static final Logger LOG = LoggerFactory.getLogger(UUIDUtilsTest.class);

    @Test
    public void testTimestamps() throws Exception {
        final UUID uuid = Generators.timeBasedGenerator().generate();

        final long unixTimestamp = uuidUtils.toUnixTimestamp(uuid);
        final long nowTime = System.currentTimeMillis();

        LOG.info("UnixMsFromUUID=" + unixTimestamp + ", nowMs=" + nowTime + ", delta=" + (nowTime - unixTimestamp));

        final long deltaNowEvent1 = Math.abs(nowTime - unixTimestamp);
        assertTrue(deltaNowEvent1 <= 5);
    }
}
