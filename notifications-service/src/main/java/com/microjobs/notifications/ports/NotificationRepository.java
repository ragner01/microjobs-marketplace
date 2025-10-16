package com.microjobs.notifications.ports;

import com.microjobs.notifications.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientIdAndStatusOrderByCreatedAtDesc(UUID recipientId, Notification.NotificationStatus status);

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId, Pageable pageable);

    List<Notification> findByRecipientIdAndTypeOrderByCreatedAtDesc(UUID recipientId, Notification.NotificationType type);

    @Query("SELECT n FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'UNREAD' ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findUnreadNotificationsByRecipient(@Param("recipientId") UUID recipientId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND n.status = 'UNREAD'")
    Long countUnreadNotificationsByRecipient(@Param("recipientId") UUID recipientId);

    @Query("SELECT n FROM Notification n WHERE n.expiresAt < :now AND n.status != 'ARCHIVED'")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now);

    @Query("SELECT n FROM Notification n WHERE n.tenantId = :tenantId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsByTenantSince(@Param("tenantId") String tenantId, @Param("since") LocalDateTime since);

    void deleteByRecipientIdAndStatus(UUID recipientId, Notification.NotificationStatus status);
}
