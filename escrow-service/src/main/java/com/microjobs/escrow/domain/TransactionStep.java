package com.microjobs.escrow.domain;

import com.microjobs.shared.domain.AggregateRoot;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction_steps", schema = "escrow")
@Getter
@Setter
@NoArgsConstructor
public class TransactionStep extends AggregateRoot {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private EscrowTransaction transaction;
    
    @Column(name = "step_name", nullable = false)
    private String stepName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepStatus status;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    public TransactionStep(EscrowTransaction transaction, String stepName, String status, String details) {
        super(transaction.getTenantId());
        this.transaction = transaction;
        this.stepName = stepName;
        this.status = StepStatus.valueOf(status.toUpperCase());
        this.details = details;
        this.startedAt = LocalDateTime.now();
    }
    
    public void markCompleted() {
        if (status != StepStatus.PENDING) {
            throw new IllegalStateException("Only pending steps can be completed");
        }
        this.status = StepStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void markFailed(String reason) {
        if (status != StepStatus.PENDING) {
            throw new IllegalStateException("Only pending steps can be failed");
        }
        this.status = StepStatus.FAILED;
        this.failureReason = reason;
        this.completedAt = LocalDateTime.now();
    }
    
    @Override
    public void validate() {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction is required");
        }
        if (stepName == null || stepName.trim().isEmpty()) {
            throw new IllegalArgumentException("Step name is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
    }
    
    public enum StepStatus {
        PENDING, COMPLETED, FAILED
    }
}
