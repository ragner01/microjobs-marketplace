package com.microjobs.auth.web;

import com.microjobs.auth.domain.User;
import com.microjobs.auth.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());
        
        User user = authService.registerUser(
            request.getTenantId(),
            request.getEmail(),
            request.getPassword(),
            request.getFirstName(),
            request.getLastName(),
            User.UserType.valueOf(request.getUserType())
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("userType", user.getUserType());
        response.put("status", user.getStatus());
        response.put("message", "User registered successfully");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());
        
        String token = authService.authenticateUser(request.getEmail(), request.getPassword());
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("message", "Login successful");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/profile/{userId}")
    public ResponseEntity<Map<String, Object>> getProfile(@PathVariable String userId) {
        User user = authService.getUserById(java.util.UUID.fromString(userId));
        
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("email", user.getEmail());
        response.put("firstName", user.getFirstName());
        response.put("lastName", user.getLastName());
        response.put("userType", user.getUserType());
        response.put("status", user.getStatus());
        response.put("emailVerified", user.getEmailVerified());
        response.put("lastLoginAt", user.getLastLoginAt());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Auth Service is running");
    }
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Auth Service");
        response.put("description", "MicroJobs Marketplace - Authentication & Authorization Service");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("architecture", "DDD + Hexagonal Architecture + JWT");
        response.put("endpoints", Map.of(
            "health", "/api/auth/health",
            "register", "POST /api/auth/register",
            "login", "POST /api/auth/login",
            "profile", "GET /api/auth/profile/{userId}"
        ));
        response.put("documentation", "See README.md for complete API documentation");
        return ResponseEntity.ok(response);
    }
    
    @Data
    public static class RegisterRequest {
        private String tenantId;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String userType;
    }
    
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }
}
