package com.excelian.mache.observable;

import com.excelian.mache.observable.coordination.CoordinationEntryEvent;

public interface MapEventListener<K> {
    void send(CoordinationEntryEvent<K> event);
}
