package com.microjobs.notifications.service;

import com.microjobs.notifications.domain.Notification;
import com.microjobs.notifications.domain.NotificationTemplate;
import com.microjobs.notifications.ports.NotificationRepository;
import com.microjobs.notifications.ports.NotificationTemplateRepository;
import com.microjobs.shared.infrastructure.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final SecurityContextHelper securityContextHelper;

    public Notification createNotification(UUID recipientId, UUID senderId, 
                                         Notification.NotificationType type, 
                                         String title, String message, 
                                         Integer priority, String metadata) {
        log.info("Creating notification for recipient: {} of type: {}", recipientId, type);
        
        String tenantId = securityContextHelper.getCurrentTenantId();
        Notification notification = new Notification(tenantId, recipientId, senderId, type, title, message);
        
        if (priority != null) {
            notification.setPriority(priority);
        }
        if (metadata != null) {
            notification.setMetadata(metadata);
        }
        
        notification.validate();
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time notification via WebSocket
        sendRealTimeNotification(savedNotification);
        
        return savedNotification;
    }

    public Notification createNotificationFromTemplate(UUID recipientId, UUID senderId,
                                                     Notification.NotificationType type,
                                                     Object... templateVariables) {
        log.info("Creating notification from template for recipient: {} of type: {}", recipientId, type);
        
        Optional<NotificationTemplate> templateOpt = templateRepository.findByTypeAndIsActiveTrue(type);
        if (templateOpt.isEmpty()) {
            throw new IllegalArgumentException("No active template found for type: " + type);
        }
        
        NotificationTemplate template = templateOpt.get();
        String title = template.processTitle(templateVariables);
        String message = template.processMessage(templateVariables);
        
        return createNotification(recipientId, senderId, type, title, message, 
                                 template.getDefaultPriority(), null);
    }

    public List<Notification> getNotificationsForUser(UUID userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, Pageable.unpaged()).getContent();
    }

    public Page<Notification> getNotificationsForUser(UUID userId, Pageable pageable) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);
    }

    public List<Notification> getUnreadNotificationsForUser(UUID userId) {
        return notificationRepository.findUnreadNotificationsByRecipient(userId);
    }

    public Long getUnreadNotificationCount(UUID userId) {
        return notificationRepository.countUnreadNotificationsByRecipient(userId);
    }

    public Notification markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    public Notification markAsArchived(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        
        notification.markAsArchived();
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndStatusOrderByCreatedAtDesc(
            userId, Notification.NotificationStatus.UNREAD);
        
        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteExpiredNotifications() {
        List<Notification> expiredNotifications = notificationRepository.findExpiredNotifications(LocalDateTime.now());
        notificationRepository.deleteAll(expiredNotifications);
        log.info("Deleted {} expired notifications", expiredNotifications.size());
    }

    public void deleteNotificationsByStatus(UUID userId, Notification.NotificationStatus status) {
        notificationRepository.deleteByRecipientIdAndStatus(userId, status);
    }

    private void sendRealTimeNotification(Notification notification) {
        try {
            // Send to user-specific queue
            String destination = "/user/" + notification.getRecipientId() + "/queue/notifications";
            messagingTemplate.convertAndSend(destination, notification);
            
            // Send to tenant-specific topic
            String tenantDestination = "/topic/notifications/" + notification.getTenantId();
            messagingTemplate.convertAndSend(tenantDestination, notification);
            
            log.debug("Sent real-time notification to user: {} via WebSocket", notification.getRecipientId());
        } catch (Exception e) {
            log.warn("Failed to send real-time notification: {}", e.getMessage());
        }
    }

    // Bulk notification methods
    public void notifyJobCreated(UUID jobId, String jobTitle, UUID clientId, List<UUID> workerIds) {
        for (UUID workerId : workerIds) {
            createNotificationFromTemplate(
                workerId, 
                clientId, 
                Notification.NotificationType.JOB_CREATED,
                jobTitle, jobId
            );
        }
    }

    public void notifyBidSubmitted(UUID jobId, String jobTitle, UUID workerId, UUID clientId) {
        createNotificationFromTemplate(
            clientId,
            workerId,
            Notification.NotificationType.BID_SUBMITTED,
            jobTitle, jobId
        );
    }

    public void notifyJobAssigned(UUID jobId, String jobTitle, UUID workerId, UUID clientId) {
        createNotificationFromTemplate(
            workerId,
            clientId,
            Notification.NotificationType.JOB_ASSIGNED,
            jobTitle, jobId
        );
    }

    public void notifyPaymentReceived(UUID amount, String currency, UUID recipientId, UUID senderId) {
        createNotificationFromTemplate(
            recipientId,
            senderId,
            Notification.NotificationType.PAYMENT_RECEIVED,
            amount, currency
        );
    }
}
