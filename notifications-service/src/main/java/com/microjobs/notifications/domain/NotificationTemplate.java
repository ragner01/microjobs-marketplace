package com.microjobs.notifications.domain;

import com.microjobs.shared.domain.AggregateRoot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "notification_templates", schema = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class NotificationTemplate extends AggregateRoot {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true)
    private Notification.NotificationType type;

    @Column(name = "title_template", nullable = false)
    private String titleTemplate;

    @Column(name = "message_template", nullable = false)
    private String messageTemplate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "default_priority", nullable = false)
    private Integer defaultPriority = 1;

    @Column(name = "description")
    private String description;

    public NotificationTemplate(String tenantId, Notification.NotificationType type, 
                               String titleTemplate, String messageTemplate) {
        super(tenantId);
        this.type = type;
        this.titleTemplate = titleTemplate;
        this.messageTemplate = messageTemplate;
        this.isActive = true;
        this.defaultPriority = 1;
    }

    public String processTitle(Object... variables) {
        return String.format(titleTemplate, variables);
    }

    public String processMessage(Object... variables) {
        return String.format(messageTemplate, variables);
    }

    public Integer getDefaultPriority() {
        return defaultPriority;
    }

    @Override
    public void validate() {
        if (type == null) {
            throw new IllegalArgumentException("Template type is required");
        }
        if (titleTemplate == null || titleTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Title template is required");
        }
        if (messageTemplate == null || messageTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("Message template is required");
        }
        if (defaultPriority == null || defaultPriority < 1 || defaultPriority > 4) {
            throw new IllegalArgumentException("Default priority must be between 1 and 4");
        }
    }
}
