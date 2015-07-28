package org.mache.coordination;

import java.util.Date;
import java.util.UUID;

import org.mache.EventType;
import org.mache.utils.UUIDUtils;

import com.fasterxml.uuid.Generators;

public class CoordinationEntryEvent<K> {

	private final K key;
	private final UUID cacheId;
	private final String entityName;
	private final UUID uniqueIdType1;
	private final EventType eventType;
	private Date uniqueIdTime;

	public CoordinationEntryEvent(UUID cacheId, final String entityName, final K key, final EventType eventType, final UUIDUtils uuidUtils) {
		this.cacheId = cacheId;
		this.entityName = entityName;
		this.eventType = eventType;
		this.key = key;
		uniqueIdType1 = Generators.timeBasedGenerator().generate(); // TimeUUID
		uniqueIdTime = new Date(uuidUtils.toUnixTimestamp(uniqueIdType1));
	}

	public UUID getCacheId() { return cacheId; }

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

	public Date getUniqueIdTime() throws Exception {
		return uniqueIdTime;
	}
}
