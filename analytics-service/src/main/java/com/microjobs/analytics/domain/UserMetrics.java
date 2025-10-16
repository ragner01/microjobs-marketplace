package com.microjobs.analytics.domain;

import com.microjobs.shared.domain.AggregateRoot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_metrics", schema = "analytics")
@Getter
@Setter
@NoArgsConstructor
public class UserMetrics extends AggregateRoot {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "user_type", nullable = false)
    private String userType;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(name = "total_jobs_posted")
    private Integer totalJobsPosted = 0;

    @Column(name = "total_jobs_completed")
    private Integer totalJobsCompleted = 0;

    @Column(name = "total_jobs_assigned")
    private Integer totalJobsAssigned = 0;

    @Column(name = "total_earnings")
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(name = "total_spent")
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "total_ratings")
    private Integer totalRatings = 0;

    @Column(name = "completion_rate")
    private Double completionRate = 0.0;

    @Column(name = "response_time_hours")
    private Double responseTimeHours = 0.0;

    @Column(name = "location")
    private String location;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "is_premium", nullable = false)
    private Boolean isPremium = false;

    public UserMetrics(String tenantId, UUID userId, String email, String userType, String status) {
        super(tenantId);
        this.userId = userId;
        this.email = email;
        this.userType = userType;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
        this.totalJobsPosted = 0;
        this.totalJobsCompleted = 0;
        this.totalJobsAssigned = 0;
        this.totalEarnings = BigDecimal.ZERO;
        this.totalSpent = BigDecimal.ZERO;
        this.averageRating = 0.0;
        this.totalRatings = 0;
        this.completionRate = 0.0;
        this.responseTimeHours = 0.0;
        this.isVerified = false;
        this.isPremium = false;
    }

    public void updateLastActive() {
        this.lastActiveAt = LocalDateTime.now();
    }

    public void addJobPosted() {
        this.totalJobsPosted++;
    }

    public void addJobCompleted() {
        this.totalJobsCompleted++;
        updateCompletionRate();
    }

    public void addJobAssigned() {
        this.totalJobsAssigned++;
    }

    public void addEarnings(BigDecimal amount) {
        this.totalEarnings = this.totalEarnings.add(amount);
    }

    public void addSpent(BigDecimal amount) {
        this.totalSpent = this.totalSpent.add(amount);
    }

    public void addRating(Double rating) {
        this.totalRatings++;
        if (this.averageRating == 0.0) {
            this.averageRating = rating;
        } else {
            this.averageRating = (this.averageRating * (this.totalRatings - 1) + rating) / this.totalRatings;
        }
    }

    private void updateCompletionRate() {
        if (this.totalJobsAssigned > 0) {
            this.completionRate = (double) this.totalJobsCompleted / this.totalJobsAssigned;
        }
    }

    @Override
    public void validate() {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userType == null || userType.trim().isEmpty()) {
            throw new IllegalArgumentException("User type is required");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
    }
}
