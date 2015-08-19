package org.mache.utils;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

public class UUIDUtils {
    /**
     * Solution taken from http://stackoverflow.com/questions/13070674/get-the-unix-timestamp-from-type-1-uuid
     *
     * @param uuid type 1 (time based) uuid
     * @return unixTimestamp of uuid
     */
    public long toUnixTimestamp(final UUID uuid) {
        //TODO which timezone is used for generation of uuid?
        final Calendar uuidEpoch = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        uuidEpoch.clear();
        uuidEpoch.set(1582, 9, 15, 0, 0, 0); // 9 = October
        final long epochMillis = uuidEpoch.getTime().getTime();

        final long time = (uuid.timestamp() / 10000L) + epochMillis;
        return time;
    }
}
