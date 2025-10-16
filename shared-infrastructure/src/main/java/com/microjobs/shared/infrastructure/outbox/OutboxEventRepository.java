package com.microjobs.shared.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = 'PENDING' AND e.nextRetryAt <= :now ORDER BY e.createdAt ASC")
    List<OutboxEvent> findPendingEventsForRetry(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM OutboxEvent e WHERE e.status = 'PUBLISHED' AND e.publishedAt < :cutoffDate")
    int deleteOldPublishedEvents(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT e FROM OutboxEvent e WHERE e.aggregateId = :aggregateId AND e.aggregateType = :aggregateType ORDER BY e.createdAt DESC")
    List<OutboxEvent> findByAggregate(@Param("aggregateId") UUID aggregateId, @Param("aggregateType") String aggregateType);
}
