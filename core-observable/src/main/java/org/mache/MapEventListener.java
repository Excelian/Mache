package org.mache;

import org.mache.coordination.CoordinationEntryEvent;

public interface MapEventListener {
    void send(CoordinationEntryEvent<?> event);
}
