package com.microjobs.notifications.domain;

import com.microjobs.shared.domain.AggregateRoot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", schema = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification extends AggregateRoot {

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "sender_id")
    private UUID senderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "priority", nullable = false)
    private Integer priority = 1; // 1=Low, 2=Medium, 3=High, 4=Urgent

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public Notification(String tenantId, UUID recipientId, UUID senderId, 
                       NotificationType type, String title, String message) {
        super(tenantId);
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.status = NotificationStatus.UNREAD;
        this.priority = 1;
    }

    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = LocalDateTime.now();
    }

    public void markAsArchived() {
        this.status = NotificationStatus.ARCHIVED;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isRead() {
        return status == NotificationStatus.READ;
    }

    public boolean isUnread() {
        return status == NotificationStatus.UNREAD;
    }

    @Override
    public void validate() {
        if (recipientId == null) {
            throw new IllegalArgumentException("Recipient ID is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Notification type is required");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message is required");
        }
        if (priority == null || priority < 1 || priority > 4) {
            throw new IllegalArgumentException("Priority must be between 1 and 4");
        }
    }

    public enum NotificationType {
        JOB_CREATED,
        JOB_ASSIGNED,
        JOB_COMPLETED,
        BID_SUBMITTED,
        BID_ACCEPTED,
        BID_REJECTED,
        PAYMENT_RECEIVED,
        PAYMENT_SENT,
        DISPUTE_OPENED,
        DISPUTE_RESOLVED,
        ESCROW_RELEASED,
        SYSTEM_MAINTENANCE,
        ACCOUNT_UPDATE,
        SECURITY_ALERT
    }

    public enum NotificationStatus {
        UNREAD,
        READ,
        ARCHIVED
    }
}
