package com.microjobs.shared.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Component
@Slf4j
public class SecurityContextHelper {

    public UUID getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            Object userId = request.getAttribute("userId");
            if (userId instanceof UUID) {
                return (UUID) userId;
            }
        }
        return null;
    }

    public String getCurrentTenantId() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            Object tenantId = request.getAttribute("tenantId");
            if (tenantId instanceof String) {
                return (String) tenantId;
            }
        }
        return null;
    }

    public String getCurrentUserType() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            Object userType = request.getAttribute("userType");
            if (userType instanceof String) {
                return (String) userType;
            }
        }
        return null;
    }

    public String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : null;
        }
        return null;
    }

    public boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }

    public boolean hasRole(String role) {
        String userType = getCurrentUserType();
        return userType != null && userType.equalsIgnoreCase(role);
    }

    public boolean isClient() {
        return hasRole("CLIENT");
    }

    public boolean isWorker() {
        return hasRole("WORKER");
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("PLATFORM");
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("Could not get current request: {}", e.getMessage());
            return null;
        }
    }
}
