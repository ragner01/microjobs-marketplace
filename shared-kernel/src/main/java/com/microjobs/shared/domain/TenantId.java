package com.microjobs.shared.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class TenantId {
    
    private String value;
    
    public TenantId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        this.value = value.trim().toLowerCase();
    }
    
    public static TenantId of(String value) {
        return new TenantId(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
