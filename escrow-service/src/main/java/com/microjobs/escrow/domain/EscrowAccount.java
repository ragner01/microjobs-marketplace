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
@Table(name = "escrow_accounts", schema = "escrow")
@Getter
@Setter
@NoArgsConstructor
public class EscrowAccount extends AggregateRoot {
    
    @Column(name = "account_holder_id", nullable = false)
    private UUID accountHolderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "balance_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "balance_currency"))
    })
    private Money balance;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;
    
    @OneToMany(mappedBy = "escrowAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LedgerEntry> ledgerEntries = new ArrayList<>();
    
    public EscrowAccount(String tenantId, UUID accountHolderId, AccountType accountType, Money initialBalance) {
        super(tenantId);
        this.accountHolderId = accountHolderId;
        this.accountType = accountType;
        this.balance = initialBalance;
        this.status = AccountStatus.ACTIVE;
    }
    
    public void deposit(Money amount, String description, UUID transactionId) {
        validateActiveAccount();
        validatePositiveAmount(amount);
        
        this.balance = this.balance.add(amount);
        addLedgerEntry(LedgerEntry.LedgerEntryType.DEBIT, amount, description, transactionId);
    }
    
    public void withdraw(Money amount, String description, UUID transactionId) {
        validateActiveAccount();
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);
        
        this.balance = this.balance.subtract(amount);
        addLedgerEntry(LedgerEntry.LedgerEntryType.CREDIT, amount, description, transactionId);
    }
    
    public void hold(Money amount, String description, UUID transactionId) {
        validateActiveAccount();
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);
        
        this.balance = this.balance.subtract(amount);
        addLedgerEntry(LedgerEntry.LedgerEntryType.CREDIT, amount, description + " (HOLD)", transactionId);
    }
    
    public void release(Money amount, String description, UUID transactionId) {
        validateActiveAccount();
        validatePositiveAmount(amount);
        
        this.balance = this.balance.add(amount);
        addLedgerEntry(LedgerEntry.LedgerEntryType.DEBIT, amount, description + " (RELEASE)", transactionId);
    }
    
    public void freeze() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Only active accounts can be frozen");
        }
        this.status = AccountStatus.FROZEN;
    }
    
    public void unfreeze() {
        if (status != AccountStatus.FROZEN) {
            throw new IllegalStateException("Only frozen accounts can be unfrozen");
        }
        this.status = AccountStatus.ACTIVE;
    }
    
    public void close() {
        if (status == AccountStatus.CLOSED) {
            throw new IllegalStateException("Account is already closed");
        }
        if (!balance.isZero()) {
            throw new IllegalStateException("Cannot close account with non-zero balance");
        }
        this.status = AccountStatus.CLOSED;
    }
    
    private void addLedgerEntry(LedgerEntry.LedgerEntryType type, Money amount, String description, UUID transactionId) {
        LedgerEntry entry = new LedgerEntry(this, type, amount, description, transactionId);
        this.ledgerEntries.add(entry);
    }
    
    private void validateActiveAccount() {
        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is not active");
        }
    }
    
    private void validatePositiveAmount(Money amount) {
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }
    
    private void validateSufficientBalance(Money amount) {
        if (balance.isLessThan(amount)) {
            throw new IllegalArgumentException("Insufficient balance");
        }
    }
    
    @Override
    public void validate() {
        if (accountHolderId == null) {
            throw new IllegalArgumentException("Account holder ID is required");
        }
        if (accountType == null) {
            throw new IllegalArgumentException("Account type is required");
        }
        if (balance == null) {
            throw new IllegalArgumentException("Balance is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
    }
    
    public enum AccountType {
        CLIENT, WORKER, PLATFORM, ESCROW_HOLD
    }
    
    public enum AccountStatus {
        ACTIVE, FROZEN, CLOSED
    }
}
