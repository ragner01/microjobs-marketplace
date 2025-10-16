package com.microjobs.auth.ports;

import com.microjobs.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndStatus(String email, User.UserStatus status);
    
    boolean existsByEmail(String email);
}
