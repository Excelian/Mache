package com.excelian.mache.vertx;

import com.excelian.mache.core.Mache;

/**
 * A REST managed mache provides access to a Mache along with contextual data to describe its lifetime.
 */
public class RestManagedMache {

    private final Mache<String, String> mache;
    private final long timeToLiveMillis;

    /**
     * Creates a new RestManagedMache for a given map.
     *
     * @param mache The mache to be managed
     * @param timeToLiveMillis The time to live in milliseconds, a time of 0 indicates not to expire this instance
     */
    public RestManagedMache(Mache<String, String> mache, long timeToLiveMillis) {
        this.mache = mache;
        this.timeToLiveMillis = timeToLiveMillis;
    }

    public Mache<String, String> getMache() {
        return mache;
    }

    public long getTimeToLiveMillis() {
        return timeToLiveMillis;
    }
}
