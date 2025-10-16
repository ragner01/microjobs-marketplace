package com.microjobs.auth.service;

import com.microjobs.auth.domain.User;
import com.microjobs.auth.ports.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    public User registerUser(String tenantId, String email, String password, 
                           String firstName, String lastName, User.UserType userType) {
        log.info("Registering new user: {}", email);
        
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        
        String passwordHash = passwordEncoder.encode(password);
        User user = new User(tenantId, email, passwordHash, firstName, lastName, userType);
        user.validate();
        
        return userRepository.save(user);
    }
    
    public String authenticateUser(String email, String password) {
        log.info("Authenticating user: {}", email);
        
        User user = userRepository.findByEmailAndStatus(email, User.UserStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        user.updateLastLogin();
        userRepository.save(user);
        
        return jwtService.generateToken(user);
    }
    
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
