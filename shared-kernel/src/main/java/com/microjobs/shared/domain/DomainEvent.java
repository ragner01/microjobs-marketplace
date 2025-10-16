package com.microjobs.shared.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public abstract class DomainEvent {
    
    private final UUID eventId;
    private final String eventType;
    private final LocalDateTime occurredAt;
    private final String tenantId;
    private final UUID aggregateId;
    private final Long aggregateVersion;
    
    protected DomainEvent(String eventType, String tenantId, UUID aggregateId, Long aggregateVersion) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now();
        this.tenantId = tenantId;
        this.aggregateId = aggregateId;
        this.aggregateVersion = aggregateVersion;
    }
}
