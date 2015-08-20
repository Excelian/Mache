package com.excelian.mache;

import com.excelian.mache.coordination.CoordinationEntryEvent;

public interface MapEventListener {
    void send(CoordinationEntryEvent<?> event);
}
