package org.mache.utils;

import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import com.fasterxml.uuid.Generators;

public class UUIDUtilsTest {
	private final UUIDUtils uuidUtils = new UUIDUtils();
	
	@Test
	public void testTimestamps() throws Exception {
		final UUID uuid = Generators.timeBasedGenerator().generate();
		
		final long unixTimestamp = uuidUtils.toUnixTimestamp(uuid);
		final long nowTime = System.currentTimeMillis();
		
		System.out.println("UnixMsFromUUID=" + unixTimestamp + ", nowMs=" + nowTime + ", delta=" + (nowTime - unixTimestamp));
		
		final long deltaNowEvent1 = Math.abs(nowTime - unixTimestamp);
		assertTrue(deltaNowEvent1 <= 5);
	}
}
