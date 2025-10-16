package com.microjobs.escrow.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class WelcomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Escrow Service");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("description", "MicroJobs Marketplace - Escrow & Payment Management Service");
        response.put("endpoints", Map.of(
            "health", "/api/escrow/health",
            "accounts", "/api/escrow/accounts",
            "transactions", "/api/escrow/transactions",
            "createAccount", "POST /api/escrow/accounts",
            "deposit", "POST /api/escrow/accounts/{id}/deposit",
            "withdraw", "POST /api/escrow/accounts/{id}/withdraw",
            "createTransaction", "POST /api/escrow/transactions",
            "completeTransaction", "PUT /api/escrow/transactions/{id}/complete"
        ));
        response.put("documentation", "See README.md for complete API documentation");
        response.put("architecture", "DDD + Hexagonal Architecture + Event Sourcing");
        response.put("paymentProviders", "Paystack, Stripe");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Escrow Service");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
