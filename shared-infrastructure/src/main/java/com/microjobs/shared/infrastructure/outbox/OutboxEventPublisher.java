package com.microjobs.shared.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {
    
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    @Transactional
    public void publishEvent(OutboxEvent event) {
        try {
            String topic = determineTopic(event.getEventType());
            String key = event.getAggregateId().toString();
            String payload = event.getEventData();
            
            kafkaTemplate.send(topic, key, payload)
                    .addCallback(
                        result -> {
                            event.markAsPublished();
                            outboxEventRepository.save(event);
                            log.info("Event published successfully: {} to topic: {}", event.getId(), topic);
                        },
                        failure -> {
                            event.markAsFailed(failure.getMessage());
                            outboxEventRepository.save(event);
                            log.error("Failed to publish event: {} to topic: {}", event.getId(), topic, failure);
                        }
                    );
            
        } catch (Exception e) {
            event.markAsFailed(e.getMessage());
            outboxEventRepository.save(event);
            log.error("Error publishing event: {}", event.getId(), e);
        }
    }
    
    @Scheduled(fixedDelay = 30000) // Run every 30 seconds
    @Transactional
    public void processPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEventsForRetry(LocalDateTime.now());
        
        for (OutboxEvent event : pendingEvents) {
            if (event.shouldRetry()) {
                publishEvent(event);
            }
        }
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    @Transactional
    public void cleanupOldEvents() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deletedCount = outboxEventRepository.deleteOldPublishedEvents(cutoffDate);
        log.info("Cleaned up {} old published events", deletedCount);
    }
    
    private String determineTopic(String eventType) {
        // Map event types to Kafka topics
        switch (eventType) {
            case "JobCreated":
            case "JobAssigned":
            case "JobCompleted":
                return "jobs.events";
            case "BidSubmitted":
            case "BidAccepted":
            case "BidRejected":
                return "bids.events";
            case "EscrowTransactionInitiated":
            case "EscrowTransactionCompleted":
                return "escrow.events";
            case "DisputeCreated":
            case "DisputeResolved":
                return "disputes.events";
            case "PaymentProcessed":
            case "PaymentFailed":
                return "payments.events";
            default:
                return "general.events";
        }
    }
}
