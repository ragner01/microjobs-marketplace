package com.microjobs.search.web;

import com.microjobs.search.domain.JobSearchDocument;
import com.microjobs.search.domain.UserSearchDocument;
import com.microjobs.search.service.SearchService;
import com.microjobs.shared.infrastructure.security.SecurityContextHelper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;
    private final SecurityContextHelper securityContextHelper;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Search Service");
        response.put("description", "MicroJobs Marketplace - Advanced Search Service with Elasticsearch");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("features", List.of(
            "Full-text search",
            "Geo-location search",
            "Advanced filtering",
            "Multi-criteria search",
            "Real-time indexing",
            "Analytics and insights"
        ));
        response.put("endpoints", Map.of(
            "searchJobs", "GET /api/search/jobs",
            "searchUsers", "GET /api/search/users",
            "indexJob", "POST /api/search/jobs/index",
            "indexUser", "POST /api/search/users/index",
            "analytics", "GET /api/search/analytics"
        ));
        response.put("documentation", "See README.md for complete API documentation");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobSearchDocument>> searchJobs(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double maxDistance,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) Boolean isUrgent,
            @RequestParam(required = false) Boolean isRemote,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }

        Page<JobSearchDocument> results = searchService.searchJobs(
            query, skills, minBudget, maxBudget, location, latitude, longitude, 
            maxDistance, statuses, isUrgent, isRemote, maxExperience,
            page, size, sortBy, sortDirection
        );

        return ResponseEntity.ok(results);
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserSearchDocument>> searchUsers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> skills,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) Double minRate,
            @RequestParam(required = false) Double maxRate,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Double maxDistance,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean isAvailable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "rating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }

        Page<UserSearchDocument> results = searchService.searchUsers(
            query, skills, userType, minRate, maxRate, location, latitude, longitude,
            maxDistance, minExperience, minRating, isAvailable,
            page, size, sortBy, sortDirection
        );

        return ResponseEntity.ok(results);
    }

    @PostMapping("/jobs/index")
    public ResponseEntity<Map<String, String>> indexJob(@Valid @RequestBody JobSearchDocument job) {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }

        searchService.indexJob(job);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Job indexed successfully");
        response.put("jobId", job.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/index")
    public ResponseEntity<Map<String, String>> indexUser(@Valid @RequestBody UserSearchDocument user) {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }

        searchService.indexUser(user);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User indexed successfully");
        response.put("userId", user.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, String>> deleteJob(@PathVariable String jobId) {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }

        searchService.deleteJob(jobId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Job deleted from index successfully");
        response.put("jobId", jobId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        UUID currentUserId = securityContextHelper.getCurrentUserId();
        if (currentUserId == null) {
            return ResponseEntity.status(403).build();
        }

        searchService.deleteUser(userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "User deleted from index successfully");
        response.put("userId", userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analytics/popular-skills")
    public ResponseEntity<List<String>> getPopularSkills(@RequestParam(defaultValue = "10") int limit) {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }

        List<String> skills = searchService.getPopularSkills(limit);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/analytics/popular-locations")
    public ResponseEntity<List<String>> getPopularLocations(@RequestParam(defaultValue = "10") int limit) {
        UUID userId = securityContextHelper.getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }

        List<String> locations = searchService.getPopularLocations(limit);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Search Service is running");
    }
}
