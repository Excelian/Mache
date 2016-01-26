package com.excelian.mache.observable;

import com.excelian.mache.observable.coordination.CoordinationEntryEvent;

/**
 * Listens to events from the map.
 * @param <K> the type of the keys
 */
public interface MapEventListener<K> {
    /**
     * Called to send the event on the message queue.
     * @param event the event to send
     */
    void send(CoordinationEntryEvent<K> event);
}
