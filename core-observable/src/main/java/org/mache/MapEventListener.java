package org.mache;

import org.mache.coordination.CoordinationEntryEvent;

/**
 * Created by neil.avery on 11/06/2015.
 */
public interface MapEventListener {
	void send(CoordinationEntryEvent<?> event);
}
