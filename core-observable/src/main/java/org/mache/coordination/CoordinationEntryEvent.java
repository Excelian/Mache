package org.mache.coordination;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import com.fasterxml.uuid.Generators;
import org.mache.EventType;

public class CoordinationEntryEvent<K> {

    private final K key;
    private final String entityName;
    private final UUID uniqueIdType1;
    private final EventType eventType;
    private final Date eventTime;

    public CoordinationEntryEvent(String entityName, K key, EventType eventType,Date timeOfEventOccurence) {
        this.entityName = entityName;
		this.eventType = eventType;
        this.key = key;
        this.eventTime=timeOfEventOccurence;
        uniqueIdType1 = Generators.timeBasedGenerator().generate(); //TimeUUID
    }

    public K getKey(){
        return key;
    }
    public String getEntityName() {
    	return entityName;
    }
    public Date getEventTime() {
        return eventTime;
    }

    public EventType getEventType(){
        return eventType;
    }

    public UUID getUniqueId() {
        return uniqueIdType1;
    }

    public Date getUniqueIdTime() throws Exception {
        throw new Exception("Not implemented need to extract date time for uuid");
    }
}
