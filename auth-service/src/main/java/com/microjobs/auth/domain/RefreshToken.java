package com.microjobs.auth.domain;

import com.microjobs.shared.domain.AggregateRoot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens", schema = "auth")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken extends AggregateRoot {
    
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_revoked", nullable = false)
    private Boolean isRevoked = false;
    
    public RefreshToken(String tenantId, String token, UUID userId, LocalDateTime expiresAt) {
        super(tenantId);
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public void revoke() {
        this.isRevoked = true;
    }
    
    public boolean isValid() {
        return !isRevoked && !isExpired();
    }
    
    @Override
    public void validate() {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("Expires at is required");
        }
    }
}
