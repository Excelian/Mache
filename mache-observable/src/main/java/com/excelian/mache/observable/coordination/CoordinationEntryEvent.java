package com.excelian.mache.observable.coordination;

import com.fasterxml.uuid.Generators;
import com.excelian.mache.observable.EventType;
import com.excelian.mache.observable.utils.UuidUtils;

import java.util.Date;
import java.util.UUID;

/**
 * Event descriptor.
 * @param <K> the type of the key
 */
public class CoordinationEntryEvent<K> {

    private final K key;
    private final UUID cacheId;
    private final String entityName;
    private final UUID uniqueIdType1;
    private final EventType eventType;
    private Date uniqueIdTime;

    /**
     * Constructor.
     * @param cacheId - cacheId
     * @param entityName - entityName
     * @param key - key
     * @param eventType - eventType
     * @param uuidUtils - uuidUtils
     */
    public CoordinationEntryEvent(UUID cacheId, final String entityName,
                                  final K key, final EventType eventType,
                                  final UuidUtils uuidUtils) {
        this.cacheId = cacheId;
        this.entityName = entityName;
        this.eventType = eventType;
        this.key = key;
        uniqueIdType1 = Generators.timeBasedGenerator().generate(); // TimeUUID
        uniqueIdTime = new Date(uuidUtils.toUnixTimestamp(uniqueIdType1));
    }

    public UUID getCacheId() {
        return cacheId;
    }

    public K getKey() {
        return key;
    }

    public String getEntityName() {
        return entityName;
    }

    public EventType getEventType() {
        return eventType;
    }

    public UUID getUniqueId() {
        return uniqueIdType1;
    }
}
