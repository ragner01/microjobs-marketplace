package com.microjobs.notifications.web;

import com.microjobs.notifications.domain.Notification;
import com.microjobs.notifications.service.NotificationService;
import com.microjobs.shared.infrastructure.security.SecurityContextHelper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;
    private final SecurityContextHelper securityContextHelper;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Notification Service");
        response.put("description", "MicroJobs Marketplace - Real-time Notification Service");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("features", List.of(
            "Real-time WebSocket notifications",
            "JWT authentication",
            "Multi-tenant support",
            "Notification templates",
            "Priority-based delivery"
        ));
        response.put("endpoints", Map.of(
            "getNotifications", "GET /api/notifications",
            "getUnreadCount", "GET /api/notifications/unread-count",
            "markAsRead", "PUT /api/notifications/{id}/read",
            "markAllAsRead", "PUT /api/notifications/read-all",
            "websocket", "ws://localhost:8102/ws"
        ));
        response.put("documentation", "See README.md for complete API documentation");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<Notification>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getNotificationsForUser(userId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }
        
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }
        
        Long count = notificationService.getUnreadNotificationCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable UUID id) {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }
        
        Notification notification = notificationService.markAsRead(id);
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }
        
        notificationService.markAllAsRead(userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Notification> markAsArchived(@PathVariable UUID id) {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }
        
        Notification notification = notificationService.markAsArchived(id);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/create")
    public ResponseEntity<Notification> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        UUID currentUserId = securityContextHelper.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(403).build();
        }
        
        Notification notification = notificationService.createNotification(
            request.getRecipientId(),
            currentUserId,
            request.getType(),
            request.getTitle(),
            request.getMessage(),
            request.getPriority(),
            request.getMetadata()
        );
        
        return ResponseEntity.ok(notification);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Notification Service is running");
    }

    @Data
    public static class CreateNotificationRequest {
        private UUID recipientId;
        private Notification.NotificationType type;
        private String title;
        private String message;
        private Integer priority;
        private String metadata;

        public UUID getRecipientId() { return recipientId; }
        public Notification.NotificationType getType() { return type; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public Integer getPriority() { return priority; }
        public String getMetadata() { return metadata; }
    }
}
