package com.microjobs.notifications.web;

import com.microjobs.notifications.domain.Notification;
import com.microjobs.notifications.service.NotificationService;
import com.microjobs.shared.infrastructure.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {

    private final NotificationService notificationService;
    private final SecurityContextHelper securityContextHelper;

    @MessageMapping("/notifications.subscribe")
    @SendTo("/topic/notifications")
    public String handleSubscription(SimpMessageHeaderAccessor headerAccessor) {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId != null) {
            log.info("User {} subscribed to notifications", userId);
            return "Subscribed to notifications for user: " + userId;
        }
        return "Subscription failed - user not authenticated";
    }

    @MessageMapping("/notifications.mark-read")
    public void handleMarkAsRead(String notificationId, SimpMessageHeaderAccessor headerAccessor) {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId != null) {
            try {
                UUID id = UUID.fromString(notificationId);
                notificationService.markAsRead(id);
                log.info("User {} marked notification {} as read", userId, id);
            } catch (Exception e) {
                log.warn("Failed to mark notification as read: {}", e.getMessage());
            }
        }
    }

    @MessageMapping("/notifications.ping")
    @SendTo("/topic/notifications.pong")
    public String handlePing() {
        return "pong";
    }
}
