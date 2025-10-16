package com.microjobs.escrow.application;

import com.microjobs.escrow.domain.*;
import com.microjobs.escrow.ports.EscrowAccountRepository;
import com.microjobs.escrow.ports.EscrowTransactionRepository;
import com.microjobs.shared.domain.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EscrowSagaOrchestrator {
    
    private final EscrowAccountRepository accountRepository;
    private final EscrowTransactionRepository transactionRepository;
    
    @Transactional
    public UUID initiateJobPayment(UUID jobId, UUID clientId, UUID workerId, Money amount, String description) {
        log.info("Initiating job payment saga for job: {}, client: {}, worker: {}, amount: {}", 
                jobId, clientId, workerId, amount);
        
        EscrowTransaction transaction = new EscrowTransaction(
            "default", // TODO: Get from context
            jobId, clientId, workerId, amount, 
            EscrowTransaction.TransactionType.JOB_PAYMENT, 
            description
        );
        
        transaction.addStep("VALIDATE_ACCOUNTS", "PENDING", "Validate client and worker accounts");
        transaction.addStep("HOLD_FUNDS", "PENDING", "Hold funds from client account");
        transaction.addStep("CREATE_ESCROW_HOLD", "PENDING", "Create escrow hold account");
        transaction.addStep("NOTIFY_PARTIES", "PENDING", "Notify client and worker");
        
        transactionRepository.save(transaction);
        
        // Start saga execution
        executeJobPaymentSaga(transaction);
        
        return transaction.getId();
    }
    
    @Transactional
    public void executeJobPaymentSaga(EscrowTransaction transaction) {
        try {
            // Step 1: Validate accounts
            validateAccounts(transaction);
            transaction.markStepCompleted("VALIDATE_ACCOUNTS");
            
            // Step 2: Hold funds
            holdFunds(transaction);
            transaction.markStepCompleted("HOLD_FUNDS");
            
            // Step 3: Create escrow hold
            createEscrowHold(transaction);
            transaction.markStepCompleted("CREATE_ESCROW_HOLD");
            
            // Step 4: Notify parties
            notifyParties(transaction);
            transaction.markStepCompleted("NOTIFY_PARTIES");
            
            transaction.complete();
            transactionRepository.save(transaction);
            
            log.info("Job payment saga completed successfully for transaction: {}", transaction.getId());
            
        } catch (Exception e) {
            log.error("Job payment saga failed for transaction: {}", transaction.getId(), e);
            transaction.fail(e.getMessage());
            transactionRepository.save(transaction);
            
            // Compensate for completed steps
            compensateJobPaymentSaga(transaction);
        }
    }
    
    @Transactional
    public void releaseJobPayment(UUID transactionId) {
        EscrowTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        log.info("Releasing job payment for transaction: {}", transactionId);
        
        try {
            // Release funds from escrow to worker
            EscrowAccount workerAccount = accountRepository.findByAccountHolderIdAndAccountType(
                transaction.getWorkerId(), EscrowAccount.AccountType.WORKER)
                .orElseThrow(() -> new IllegalArgumentException("Worker account not found"));
            
            EscrowAccount escrowHoldAccount = accountRepository.findByAccountHolderIdAndAccountType(
                transaction.getId(), EscrowAccount.AccountType.ESCROW_HOLD)
                .orElseThrow(() -> new IllegalArgumentException("Escrow hold account not found"));
            
            // Transfer from escrow hold to worker account
            escrowHoldAccount.release(transaction.getAmount(), 
                "Job payment release for job: " + transaction.getJobId(), transactionId);
            workerAccount.deposit(transaction.getAmount(), 
                "Job payment received for job: " + transaction.getJobId(), transactionId);
            
            accountRepository.save(escrowHoldAccount);
            accountRepository.save(workerAccount);
            
            log.info("Job payment released successfully for transaction: {}", transactionId);
            
        } catch (Exception e) {
            log.error("Failed to release job payment for transaction: {}", transactionId, e);
            throw e;
        }
    }
    
    @Transactional
    public void refundJobPayment(UUID transactionId, Money refundAmount, String reason) {
        EscrowTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        log.info("Refunding job payment for transaction: {}, amount: {}, reason: {}", 
                transactionId, refundAmount, reason);
        
        try {
            EscrowAccount clientAccount = accountRepository.findByAccountHolderIdAndAccountType(
                transaction.getClientId(), EscrowAccount.AccountType.CLIENT)
                .orElseThrow(() -> new IllegalArgumentException("Client account not found"));
            
            EscrowAccount escrowHoldAccount = accountRepository.findByAccountHolderIdAndAccountType(
                transaction.getId(), EscrowAccount.AccountType.ESCROW_HOLD)
                .orElseThrow(() -> new IllegalArgumentException("Escrow hold account not found"));
            
            // Transfer from escrow hold back to client account
            escrowHoldAccount.release(refundAmount, 
                "Job payment refund for job: " + transaction.getJobId() + " - " + reason, transactionId);
            clientAccount.deposit(refundAmount, 
                "Job payment refund for job: " + transaction.getJobId() + " - " + reason, transactionId);
            
            accountRepository.save(escrowHoldAccount);
            accountRepository.save(clientAccount);
            
            log.info("Job payment refunded successfully for transaction: {}", transactionId);
            
        } catch (Exception e) {
            log.error("Failed to refund job payment for transaction: {}", transactionId, e);
            throw e;
        }
    }
    
    private void validateAccounts(EscrowTransaction transaction) {
        EscrowAccount clientAccount = accountRepository.findByAccountHolderIdAndAccountType(
            transaction.getClientId(), EscrowAccount.AccountType.CLIENT)
            .orElseThrow(() -> new IllegalArgumentException("Client account not found"));
        
        EscrowAccount workerAccount = accountRepository.findByAccountHolderIdAndAccountType(
            transaction.getWorkerId(), EscrowAccount.AccountType.WORKER)
            .orElseThrow(() -> new IllegalArgumentException("Worker account not found"));
        
        if (clientAccount.getStatus() != EscrowAccount.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Client account is not active");
        }
        
        if (workerAccount.getStatus() != EscrowAccount.AccountStatus.ACTIVE) {
            throw new IllegalStateException("Worker account is not active");
        }
        
        if (clientAccount.getBalance().isLessThan(transaction.getAmount())) {
            throw new IllegalStateException("Insufficient client balance");
        }
    }
    
    private void holdFunds(EscrowTransaction transaction) {
        EscrowAccount clientAccount = accountRepository.findByAccountHolderIdAndAccountType(
            transaction.getClientId(), EscrowAccount.AccountType.CLIENT)
            .orElseThrow(() -> new IllegalArgumentException("Client account not found"));
        
        clientAccount.hold(transaction.getAmount(), 
            "Job payment hold for job: " + transaction.getJobId(), transaction.getId());
        
        accountRepository.save(clientAccount);
    }
    
    private void createEscrowHold(EscrowTransaction transaction) {
        EscrowAccount escrowHoldAccount = new EscrowAccount(
            transaction.getTenantId(),
            transaction.getId(), // Use transaction ID as account holder ID for escrow hold
            EscrowAccount.AccountType.ESCROW_HOLD,
            transaction.getAmount()
        );
        
        accountRepository.save(escrowHoldAccount);
    }
    
    private void notifyParties(EscrowTransaction transaction) {
        // TODO: Implement notification logic
        log.info("Notifying parties about escrow transaction: {}", transaction.getId());
    }
    
    private void compensateJobPaymentSaga(EscrowTransaction transaction) {
        log.info("Compensating job payment saga for transaction: {}", transaction.getId());
        
        try {
            // Release any held funds back to client
            EscrowAccount clientAccount = accountRepository.findByAccountHolderIdAndAccountType(
                transaction.getClientId(), EscrowAccount.AccountType.CLIENT)
                .orElse(null);
            
            if (clientAccount != null) {
                clientAccount.release(transaction.getAmount(), 
                    "Compensation for failed job payment saga", transaction.getId());
                accountRepository.save(clientAccount);
            }
            
            // Clean up escrow hold account if it exists
            accountRepository.findByAccountHolderIdAndAccountType(
                transaction.getId(), EscrowAccount.AccountType.ESCROW_HOLD)
                .ifPresent(accountRepository::delete);
            
        } catch (Exception e) {
            log.error("Failed to compensate job payment saga for transaction: {}", transaction.getId(), e);
        }
    }
}
