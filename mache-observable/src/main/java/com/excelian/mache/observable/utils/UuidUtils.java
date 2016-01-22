package com.excelian.mache.observable.utils;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import static java.util.Calendar.OCTOBER;

/**
 * Creates a timestamp from a UUID identifier.
 */
public class UuidUtils {
    /**
     * Solution taken from http://stackoverflow.com/questions/13070674/get-the-unix-timestamp-from-type-1-uuid
     *
     * @param uuid type 1 (time based) uuid
     * @return unixTimestamp of uuid
     */
    public long toUnixTimestamp(final UUID uuid) {
        final Calendar uuidEpoch = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        uuidEpoch.clear();
        uuidEpoch.set(1582, OCTOBER, 15, 0, 0, 0);
        final long epochMillis = uuidEpoch.getTime().getTime();

        return (uuid.timestamp() / 10000L) + epochMillis;
    }
}
