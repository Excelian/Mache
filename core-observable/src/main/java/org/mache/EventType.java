package org.mache;

/**
 * The type of event received by the listener.
 *
 */
public enum EventType {

    /**
     * An event type indicating that the cache entry was created.
     */
    CREATED,

    /**
     * An event type indicating that the cache entry was updated. i.e. a previous
     * mapping existed
     */
    UPDATED,


    /**
     * An event type indicating that the cache entry was removed.
     */
    REMOVED,


    /**
     * An event type indicating that the cache entry has expired or been invalidated.
     */
    INVALIDATE

}
