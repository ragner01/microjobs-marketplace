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
@Table(name = "platform_metrics", schema = "analytics")
@Getter
@Setter
@NoArgsConstructor
public class PlatformMetrics extends AggregateRoot {

    @Column(name = "metric_date", nullable = false)
    private LocalDateTime metricDate;

    @Column(name = "total_users", nullable = false)
    private Integer totalUsers = 0;

    @Column(name = "active_users", nullable = false)
    private Integer activeUsers = 0;

    @Column(name = "total_jobs", nullable = false)
    private Integer totalJobs = 0;

    @Column(name = "active_jobs", nullable = false)
    private Integer activeJobs = 0;

    @Column(name = "completed_jobs", nullable = false)
    private Integer completedJobs = 0;

    @Column(name = "total_transactions", nullable = false)
    private Integer totalTransactions = 0;

    @Column(name = "total_volume", nullable = false)
    private BigDecimal totalVolume = BigDecimal.ZERO;

    @Column(name = "average_job_value", nullable = false)
    private BigDecimal averageJobValue = BigDecimal.ZERO;

    @Column(name = "platform_fee", nullable = false)
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "job_completion_rate", nullable = false)
    private Double jobCompletionRate = 0.0;

    @Column(name = "average_response_time_hours", nullable = false)
    private Double averageResponseTimeHours = 0.0;

    @Column(name = "user_satisfaction_score", nullable = false)
    private Double userSatisfactionScore = 0.0;

    @Column(name = "new_users_today", nullable = false)
    private Integer newUsersToday = 0;

    @Column(name = "new_jobs_today", nullable = false)
    private Integer newJobsToday = 0;

    @Column(name = "revenue_today", nullable = false)
    private BigDecimal revenueToday = BigDecimal.ZERO;

    public PlatformMetrics(String tenantId, LocalDateTime metricDate) {
        super(tenantId);
        this.metricDate = metricDate;
        this.totalUsers = 0;
        this.activeUsers = 0;
        this.totalJobs = 0;
        this.activeJobs = 0;
        this.completedJobs = 0;
        this.totalTransactions = 0;
        this.totalVolume = BigDecimal.ZERO;
        this.averageJobValue = BigDecimal.ZERO;
        this.platformFee = BigDecimal.ZERO;
        this.jobCompletionRate = 0.0;
        this.averageResponseTimeHours = 0.0;
        this.userSatisfactionScore = 0.0;
        this.newUsersToday = 0;
        this.newJobsToday = 0;
        this.revenueToday = BigDecimal.ZERO;
    }

    public void updateJobMetrics(Integer totalJobs, Integer activeJobs, Integer completedJobs) {
        this.totalJobs = totalJobs;
        this.activeJobs = activeJobs;
        this.completedJobs = completedJobs;
        
        if (totalJobs > 0) {
            this.jobCompletionRate = (double) completedJobs / totalJobs;
        }
    }

    public void updateUserMetrics(Integer totalUsers, Integer activeUsers) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
    }

    public void updateTransactionMetrics(Integer totalTransactions, BigDecimal totalVolume) {
        this.totalTransactions = totalTransactions;
        this.totalVolume = totalVolume;
        
        if (totalJobs > 0) {
            this.averageJobValue = totalVolume.divide(BigDecimal.valueOf(totalJobs));
        }
    }

    public void updateDailyMetrics(Integer newUsersToday, Integer newJobsToday, BigDecimal revenueToday) {
        this.newUsersToday = newUsersToday;
        this.newJobsToday = newJobsToday;
        this.revenueToday = revenueToday;
    }

    @Override
    public void validate() {
        if (metricDate == null) {
            throw new IllegalArgumentException("Metric date is required");
        }
        if (totalUsers < 0) {
            throw new IllegalArgumentException("Total users cannot be negative");
        }
        if (activeUsers < 0 || activeUsers > totalUsers) {
            throw new IllegalArgumentException("Active users cannot be negative or greater than total users");
        }
        if (totalJobs < 0) {
            throw new IllegalArgumentException("Total jobs cannot be negative");
        }
        if (activeJobs < 0 || activeJobs > totalJobs) {
            throw new IllegalArgumentException("Active jobs cannot be negative or greater than total jobs");
        }
        if (completedJobs < 0 || completedJobs > totalJobs) {
            throw new IllegalArgumentException("Completed jobs cannot be negative or greater than total jobs");
        }
    }
}
