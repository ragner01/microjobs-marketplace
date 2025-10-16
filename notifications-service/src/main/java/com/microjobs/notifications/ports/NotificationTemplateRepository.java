package com.microjobs.notifications.ports;

import com.microjobs.notifications.domain.Notification;
import com.microjobs.notifications.domain.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByTypeAndIsActiveTrue(Notification.NotificationType type);

    Optional<NotificationTemplate> findByType(Notification.NotificationType type);

    List<NotificationTemplate> findByIsActiveTrue();
}
