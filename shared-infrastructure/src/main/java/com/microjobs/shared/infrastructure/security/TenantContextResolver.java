package com.microjobs.shared.infrastructure.security;

import com.microjobs.shared.domain.TenantId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TenantContextResolver {
    
    private static final String TENANT_ID_CLAIM = "tenant_id";
    private static final String PREFERRED_USERNAME_CLAIM = "preferred_username";
    private static final String ROLES_CLAIM = "realm_access";
    
    public Optional<TenantId> getCurrentTenant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            
            // Try to get tenant ID from custom claim
            String tenantId = jwt.getClaimAsString(TENANT_ID_CLAIM);
            if (tenantId != null && !tenantId.isEmpty()) {
                return Optional.of(TenantId.of(tenantId));
            }
            
            // Fallback: extract tenant from username (e.g., user@tenant.com)
            String username = jwt.getClaimAsString(PREFERRED_USERNAME_CLAIM);
            if (username != null && username.contains("@")) {
                String[] parts = username.split("@");
                if (parts.length == 2) {
                    return Optional.of(TenantId.of(parts[1]));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<String> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return Optional.ofNullable(jwt.getClaimAsString("sub"));
        }
        
        return Optional.empty();
    }
    
    public Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            return Optional.ofNullable(jwt.getClaimAsString(PREFERRED_USERNAME_CLAIM));
        }
        
        return Optional.empty();
    }
    
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            
            // Check realm roles
            Object realmAccess = jwt.getClaim(ROLES_CLAIM);
            if (realmAccess instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> realmAccessMap = (java.util.Map<String, Object>) realmAccess;
                Object roles = realmAccessMap.get("roles");
                if (roles instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> rolesList = (java.util.List<String>) roles;
                    return rolesList.contains(role);
                }
            }
            
            // Check resource roles
            Object resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess instanceof java.util.Map) {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> resourceAccessMap = (java.util.Map<String, Object>) resourceAccess;
                for (Object resource : resourceAccessMap.values()) {
                    if (resource instanceof java.util.Map) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> resourceMap = (java.util.Map<String, Object>) resource;
                        Object roles = resourceMap.get("roles");
                        if (roles instanceof java.util.List) {
                            @SuppressWarnings("unchecked")
                            java.util.List<String> rolesList = (java.util.List<String>) roles;
                            if (rolesList.contains(role)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    public boolean isAdmin() {
        return hasRole("admin") || hasRole("tenant-admin");
    }
    
    public boolean isClient() {
        return hasRole("client");
    }
    
    public boolean isWorker() {
        return hasRole("worker");
    }
}
