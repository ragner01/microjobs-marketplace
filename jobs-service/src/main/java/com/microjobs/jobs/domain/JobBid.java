package com.microjobs.jobs.domain;

import com.microjobs.shared.domain.AggregateRoot;
import com.microjobs.shared.domain.Money;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_bids", schema = "jobs")
@Getter
@Setter
@NoArgsConstructor
public class JobBid extends AggregateRoot {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;
    
    @Column(name = "worker_id", nullable = false)
    private UUID workerId;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "bid_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "bid_currency"))
    })
    private Money bidAmount;
    
    @Column(name = "proposal", columnDefinition = "TEXT")
    private String proposal;
    
    @Column(name = "estimated_completion_days")
    private Integer estimatedCompletionDays;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BidStatus status;
    
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;
    
    public JobBid(Job job, UUID workerId, Money bidAmount, String proposal, 
                  Integer estimatedCompletionDays) {
        super(job.getTenantId());
        this.job = job;
        this.workerId = workerId;
        this.bidAmount = bidAmount;
        this.proposal = proposal;
        this.estimatedCompletionDays = estimatedCompletionDays;
        this.status = BidStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }
    
    public void accept() {
        if (status != BidStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted bids can be accepted");
        }
        this.status = BidStatus.ACCEPTED;
    }
    
    public void reject() {
        if (status != BidStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted bids can be rejected");
        }
        this.status = BidStatus.REJECTED;
    }
    
    public void withdraw() {
        if (status != BidStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted bids can be withdrawn");
        }
        this.status = BidStatus.WITHDRAWN;
    }
    
    @Override
    public void validate() {
        if (job == null) {
            throw new IllegalArgumentException("Job is required");
        }
        if (workerId == null) {
            throw new IllegalArgumentException("Worker ID is required");
        }
        if (bidAmount == null || bidAmount.isZero()) {
            throw new IllegalArgumentException("Bid amount must be greater than zero");
        }
        if (bidAmount.isGreaterThan(job.getBudget())) {
            throw new IllegalArgumentException("Bid amount cannot exceed job budget");
        }
        if (estimatedCompletionDays != null && estimatedCompletionDays <= 0) {
            throw new IllegalArgumentException("Estimated completion days must be positive");
        }
    }
    
    public enum BidStatus {
        SUBMITTED, ACCEPTED, REJECTED, WITHDRAWN
    }
}
