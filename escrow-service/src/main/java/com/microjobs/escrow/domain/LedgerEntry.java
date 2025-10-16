package com.microjobs.escrow.domain;

import com.microjobs.shared.domain.AggregateRoot;
import com.microjobs.shared.domain.Money;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries", schema = "escrow")
@Getter
@Setter
@NoArgsConstructor
public class LedgerEntry extends AggregateRoot {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "escrow_account_id", nullable = false)
    private EscrowAccount escrowAccount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType entryType;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "currency"))
    })
    private Money amount;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;
    
    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;
    
    @Column(name = "reference_number")
    private String referenceNumber;
    
    public LedgerEntry(EscrowAccount escrowAccount, LedgerEntryType entryType, Money amount, 
                      String description, UUID transactionId) {
        super(escrowAccount.getTenantId());
        this.escrowAccount = escrowAccount;
        this.entryType = entryType;
        this.amount = amount;
        this.description = description;
        this.transactionId = transactionId;
        this.postedAt = LocalDateTime.now();
        this.referenceNumber = generateReferenceNumber();
    }
    
    private String generateReferenceNumber() {
        return String.format("LE-%s-%d", 
            postedAt.toLocalDate().toString().replace("-", ""),
            System.currentTimeMillis() % 10000);
    }
    
    @Override
    public void validate() {
        if (escrowAccount == null) {
            throw new IllegalArgumentException("Escrow account is required");
        }
        if (entryType == null) {
            throw new IllegalArgumentException("Entry type is required");
        }
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description is required");
        }
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
    }
    
    public enum LedgerEntryType {
        DEBIT, CREDIT
    }
}
