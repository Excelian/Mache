package org.mache.coordination;

import java.util.Date;
import java.util.UUID;

import org.mache.EventType;

public class CoordinationEntryEvent<K> {

    private final K key;
    private final String entityName;
    private final String uniqueId;
    private final EventType eventType;
    private final Date eventTime;

    public CoordinationEntryEvent(String entityName, K key, EventType eventType,Date timeOfEventOccurence) {
        this.entityName = entityName;
		this.eventType = eventType;
        this.key = key;
        this.eventTime=timeOfEventOccurence;

        uniqueId= UUID.randomUUID().toString();//TODO: replace with Type 1 UUID to enable tracking of time this message is constructed
    }

    public K getKey(){
        return key;
    }
    public String getEntityName() {
    	return entityName;
    }
    public Date getEventTime(){
        return eventTime;
    }
    public EventType getEventType(){
        return eventType;
    }

    public String getUniqueId() {
        return uniqueId;
    }
}
