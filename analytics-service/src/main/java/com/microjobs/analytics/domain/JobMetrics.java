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
@Table(name = "job_metrics", schema = "analytics")
@Getter
@Setter
@NoArgsConstructor
public class JobMetrics extends AggregateRoot {

    @Column(name = "job_id", nullable = false)
    private UUID jobId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "worker_id")
    private UUID workerId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "category")
    private String category;

    @Column(name = "budget_amount", nullable = false)
    private BigDecimal budgetAmount;

    @Column(name = "budget_currency", nullable = false)
    private String budgetCurrency;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "bid_count", nullable = false)
    private Integer bidCount = 0;

    @Column(name = "average_bid_amount")
    private BigDecimal averageBidAmount;

    @Column(name = "completion_time_hours")
    private Long completionTimeHours;

    @Column(name = "client_rating")
    private Double clientRating;

    @Column(name = "worker_rating")
    private Double workerRating;

    @Column(name = "is_urgent", nullable = false)
    private Boolean isUrgent = false;

    @Column(name = "is_remote", nullable = false)
    private Boolean isRemote = false;

    @Column(name = "location")
    private String location;

    public JobMetrics(String tenantId, UUID jobId, UUID clientId, String title, 
                     String category, BigDecimal budgetAmount, String budgetCurrency, 
                     String status) {
        super(tenantId);
        this.jobId = jobId;
        this.clientId = clientId;
        this.title = title;
        this.category = category;
        this.budgetAmount = budgetAmount;
        this.budgetCurrency = budgetCurrency;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.bidCount = 0;
        this.isUrgent = false;
        this.isRemote = false;
    }

    public void updateStatus(String newStatus) {
        this.status = newStatus;
        if ("ASSIGNED".equals(newStatus)) {
            this.assignedAt = LocalDateTime.now();
        } else if ("COMPLETED".equals(newStatus)) {
            this.completedAt = LocalDateTime.now();
            if (this.assignedAt != null) {
                this.completionTimeHours = java.time.Duration.between(this.assignedAt, this.completedAt).toHours();
            }
        }
    }

    public void addBid(BigDecimal bidAmount) {
        this.bidCount++;
        if (this.averageBidAmount == null) {
            this.averageBidAmount = bidAmount;
        } else {
            this.averageBidAmount = this.averageBidAmount.add(bidAmount).divide(BigDecimal.valueOf(2));
        }
    }

    @Override
    public void validate() {
        if (jobId == null) {
            throw new IllegalArgumentException("Job ID is required");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID is required");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (budgetAmount == null || budgetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Budget amount must be greater than 0");
        }
        if (budgetCurrency == null || budgetCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Budget currency is required");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status is required");
        }
    }
}
