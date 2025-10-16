package com.microjobs.analytics.web;

import com.microjobs.analytics.service.AnalyticsService;
import com.microjobs.shared.infrastructure.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final SecurityContextHelper securityContextHelper;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Analytics Service");
        response.put("description", "MicroJobs Marketplace - Analytics and Reporting Service");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("features", new String[]{
            "Job analytics and metrics",
            "User analytics and KPIs",
            "Platform performance metrics",
            "Real-time dashboard data",
            "Historical trend analysis",
            "Business intelligence reports"
        });
        response.put("endpoints", Map.of(
            "jobAnalytics", "GET /api/analytics/jobs",
            "userAnalytics", "GET /api/analytics/users",
            "platformAnalytics", "GET /api/analytics/platform",
            "kpiDashboard", "GET /api/analytics/dashboard",
            "recordJobCreated", "POST /api/analytics/jobs/record",
            "recordUserActivity", "POST /api/analytics/users/record"
        ));
        response.put("documentation", "See README.md for complete API documentation");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs")
    public ResponseEntity<Map<String, Object>> getJobAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        String tenantId = securityContextHelper.getCurrentTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> analytics = analyticsService.getJobAnalytics(tenantId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUserAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        String tenantId = securityContextHelper.getCurrentTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> analytics = analyticsService.getUserAnalytics(tenantId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/platform")
    public ResponseEntity<Map<String, Object>> getPlatformAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        String tenantId = securityContextHelper.getCurrentTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        Map<String, Object> analytics = analyticsService.getPlatformAnalytics(tenantId, startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getKPIDashboard() {
        String tenantId = securityContextHelper.getCurrentTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        Map<String, Object> dashboard = analyticsService.getKPIDashboard(tenantId);
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/jobs/record")
    public ResponseEntity<Map<String, String>> recordJobCreated(@RequestBody RecordJobRequest request) {
        String tenantId = securityContextHelper.getCurrentTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        analyticsService.recordJobCreated(
            request.getJobId(),
            request.getClientId(),
            request.getTitle(),
            request.getCategory(),
            request.getBudgetAmount(),
            request.getBudgetCurrency(),
            request.getLocation()
        );

        Map<String, String> response = new HashMap<>();
        response.put("message", "Job metrics recorded successfully");
        response.put("jobId", request.getJobId().toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/jobs/{jobId}/status")
    public ResponseEntity<Map<String, String>> recordJobStatusChange(
            @PathVariable UUID jobId, @RequestBody Map<String, String> statusRequest) {
        String tenantId = securityContextHelper.getCurrentTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        String newStatus = statusRequest.get("status");
        analyticsService.recordJobStatusChange(jobId, newStatus);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Job status metrics updated successfully");
        response.put("jobId", jobId.toString());
        response.put("newStatus", newStatus);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/record")
    public ResponseEntity<Map<String, String>> recordUserActivity(@RequestBody RecordUserRequest request) {
        String tenantId = securityContextHelper.getCurrentTenantId();
        if (tenantId == null) {
            return ResponseEntity.status(403).build();
        }

        analyticsService.recordUserActivity(
            request.getUserId(),
            request.getUserType(),
            request.getEmail()
        );

        Map<String, String> response = new HashMap<>();
        response.put("message", "User activity recorded successfully");
        response.put("userId", request.getUserId().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Analytics Service is running");
    }

    // Request DTOs
    public static class RecordJobRequest {
        private UUID jobId;
        private UUID clientId;
        private String title;
        private String category;
        private BigDecimal budgetAmount;
        private String budgetCurrency;
        private String location;

        // Getters and setters
        public UUID getJobId() { return jobId; }
        public void setJobId(UUID jobId) { this.jobId = jobId; }
        public UUID getClientId() { return clientId; }
        public void setClientId(UUID clientId) { this.clientId = clientId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getBudgetAmount() { return budgetAmount; }
        public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }
        public String getBudgetCurrency() { return budgetCurrency; }
        public void setBudgetCurrency(String budgetCurrency) { this.budgetCurrency = budgetCurrency; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }

    public static class RecordUserRequest {
        private UUID userId;
        private String userType;
        private String email;

        // Getters and setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
