package com.microjobs.jobs.web;

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
        response.put("service", "Jobs Service");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("description", "MicroJobs Marketplace - Jobs Management Service");
        response.put("endpoints", Map.of(
            "health", "/api/jobs/health",
            "jobs", "/api/jobs",
            "createJob", "POST /api/jobs",
            "getJob", "GET /api/jobs/{id}",
            "submitBid", "POST /api/jobs/{jobId}/bids",
            "assignJob", "PUT /api/jobs/{jobId}/assign/{bidId}",
            "completeJob", "PUT /api/jobs/{jobId}/complete"
        ));
        response.put("documentation", "See README.md for complete API documentation");
        response.put("architecture", "DDD + Hexagonal Architecture + Event Sourcing");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Jobs Service");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
