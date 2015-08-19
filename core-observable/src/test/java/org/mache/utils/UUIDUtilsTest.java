package org.mache.utils;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.uuid.Generators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
