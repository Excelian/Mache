package org.mache.utils;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.uuid.Generators;

public class UUIDUtilsTest {
	private final UUIDUtils uuidUtils = new UUIDUtils();
	
	@Test
	public void testTimestamps() throws Exception {
		final UUID uuid = Generators.timeBasedGenerator().generate();
		final Date now = new Date();
		
		final long unixTimestamp = uuidUtils.toUnixTimestamp(uuid);
		
		final long deltaNowEvent1 = Math.abs(now.getTime() - unixTimestamp);
		assertTrue(deltaNowEvent1 <= 1);
	}
}
