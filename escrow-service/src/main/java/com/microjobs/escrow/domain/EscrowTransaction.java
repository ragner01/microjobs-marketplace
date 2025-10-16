package com.microjobs.escrow.domain;

import com.microjobs.shared.domain.AggregateRoot;
import com.microjobs.shared.domain.Money;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "escrow_transactions", schema = "escrow")
@Getter
@Setter
@NoArgsConstructor
public class EscrowTransaction extends AggregateRoot {
    
    @Column(name = "job_id", nullable = false)
    private UUID jobId;
    
    @Column(name = "client_id", nullable = false)
    private UUID clientId;
    
    @Column(name = "worker_id", nullable = false)
    private UUID workerId;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money amount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "initiated_at", nullable = false)
    private LocalDateTime initiatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionStep> steps = new ArrayList<>();
    
    public EscrowTransaction(String tenantId, UUID jobId, UUID clientId, UUID workerId, 
                           Money amount, TransactionType type, String description) {
        super(tenantId);
        this.jobId = jobId;
        this.clientId = clientId;
        this.workerId = workerId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.status = TransactionStatus.PENDING;
        this.initiatedAt = LocalDateTime.now();
    }
    
    public void addStep(String stepName, String status, String details) {
        TransactionStep step = new TransactionStep(this, stepName, status, details);
        this.steps.add(step);
    }
    
    public void markStepCompleted(String stepName) {
        TransactionStep step = findStepByName(stepName);
        if (step != null) {
            step.markCompleted();
        }
    }
    
    public void markStepFailed(String stepName, String reason) {
        TransactionStep step = findStepByName(stepName);
        if (step != null) {
            step.markFailed(reason);
        }
    }
    
    public void complete() {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only pending transactions can be completed");
        }
        this.status = TransactionStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void fail(String reason) {
        if (status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only pending transactions can be failed");
        }
        this.status = TransactionStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        if (status == TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed transaction");
        }
        this.status = TransactionStatus.CANCELLED;
        this.completedAt = LocalDateTime.now();
    }
    
    private TransactionStep findStepByName(String stepName) {
        return steps.stream()
                .filter(step -> step.getStepName().equals(stepName))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public void validate() {
        if (jobId == null) {
            throw new IllegalArgumentException("Job ID is required");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID is required");
        }
        if (workerId == null) {
            throw new IllegalArgumentException("Worker ID is required");
        }
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type is required");
        }
    }
    
    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
    
    public enum TransactionType {
        JOB_PAYMENT, DISPUTE_REFUND, PLATFORM_FEE, PENALTY
    }
}
