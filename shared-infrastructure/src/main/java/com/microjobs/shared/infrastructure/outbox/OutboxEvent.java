package com.microjobs.shared.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events", schema = "shared")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class OutboxEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    
    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;
    
    @Column(name = "event_type", nullable = false)
    private String eventType;
    
    @Column(name = "event_data", columnDefinition = "TEXT", nullable = false)
    private String eventData;
    
    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", nullable = false)
    private Integer maxRetries = 3;
    
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public OutboxEvent(UUID aggregateId, String aggregateType, String eventType, 
                      String eventData, String tenantId) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.eventData = eventData;
        this.tenantId = tenantId;
        this.status = EventStatus.PENDING;
        this.nextRetryAt = LocalDateTime.now().plusMinutes(1);
    }
    
    public void markAsPublished() {
        this.status = EventStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String reason) {
        this.retryCount++;
        this.failureReason = reason;
        
        if (this.retryCount >= this.maxRetries) {
            this.status = EventStatus.FAILED;
        } else {
            this.status = EventStatus.PENDING;
            this.nextRetryAt = LocalDateTime.now().plusMinutes(calculateRetryDelay());
        }
    }
    
    public boolean shouldRetry() {
        return status == EventStatus.PENDING && 
               retryCount < maxRetries && 
               nextRetryAt.isBefore(LocalDateTime.now());
    }
    
    private long calculateRetryDelay() {
        // Exponential backoff: 1, 2, 4, 8 minutes
        return (long) Math.pow(2, retryCount);
    }
    
    public enum EventStatus {
        PENDING, PUBLISHED, FAILED
    }
}
