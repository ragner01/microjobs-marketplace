package com.microjobs.escrow.web;

import com.microjobs.escrow.domain.EscrowAccount;
import com.microjobs.escrow.domain.EscrowTransaction;
import com.microjobs.escrow.domain.EscrowAccount.AccountStatus;
import com.microjobs.escrow.domain.TransactionStatus;
import com.microjobs.escrow.ports.EscrowAccountRepository;
import com.microjobs.escrow.ports.EscrowTransactionRepository;
import com.microjobs.shared.domain.Money;
import com.microjobs.shared.domain.TenantId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/escrow")
@RequiredArgsConstructor
@Slf4j
public class EscrowController {

    private final EscrowAccountRepository escrowAccountRepository;
    private final EscrowTransactionRepository escrowTransactionRepository;

    @PostMapping("/accounts")
    public ResponseEntity<EscrowAccount> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("Creating escrow account for: {}", request.getAccountHolderId());
        
        EscrowAccount account = new EscrowAccount(
            request.getTenantId(),
            UUID.fromString(request.getAccountHolderId()),
            EscrowAccount.AccountType.valueOf(request.getAccountType().name()),
            new Money(java.math.BigDecimal.ZERO, java.util.Currency.getInstance("USD"))
        );
        
        EscrowAccount savedAccount = escrowAccountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<EscrowAccount> getAccount(@PathVariable UUID id) {
        return escrowAccountRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<EscrowAccount>> getAccounts(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String accountHolderId) {
        
        List<EscrowAccount> accounts;
        if (tenantId != null) {
            accounts = escrowAccountRepository.findByTenantId(tenantId);
        } else if (accountHolderId != null) {
            accounts = escrowAccountRepository.findByAccountHolderId(accountHolderId);
        } else {
            accounts = escrowAccountRepository.findAll();
        }
        
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<EscrowAccount> deposit(@PathVariable UUID accountId, @Valid @RequestBody DepositRequest request) {
        log.info("Depositing {} {} to account {}", request.getAmount(), request.getCurrency(), accountId);
        
        EscrowAccount account = escrowAccountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        Money amount = new Money(request.getAmount(), request.getCurrency());
        account.deposit(amount, request.getDescription(), request.getTransactionId());
        
        EscrowAccount savedAccount = escrowAccountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<EscrowAccount> withdraw(@PathVariable UUID accountId, @Valid @RequestBody WithdrawRequest request) {
        log.info("Withdrawing {} {} from account {}", request.getAmount(), request.getCurrency(), accountId);
        
        EscrowAccount account = escrowAccountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        
        Money amount = new Money(request.getAmount(), request.getCurrency());
        account.withdraw(amount, request.getDescription(), request.getTransactionId());
        
        EscrowAccount savedAccount = escrowAccountRepository.save(account);
        return ResponseEntity.ok(savedAccount);
    }

    @PostMapping("/transactions")
    public ResponseEntity<EscrowTransaction> createTransaction(@Valid @RequestBody CreateTransactionRequest request) {
        log.info("Creating escrow transaction for job: {}", request.getJobId());
        
        EscrowTransaction transaction = new EscrowTransaction(
            request.getTenantId(),
            request.getJobId(),
            UUID.fromString(request.getClientId()),
            UUID.fromString(request.getWorkerId()),
            new Money(request.getAmount(), request.getCurrency()),
            request.getType(),
            request.getDescription()
        );
        
        EscrowTransaction savedTransaction = escrowTransactionRepository.save(transaction);
        return ResponseEntity.ok(savedTransaction);
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<EscrowTransaction> getTransaction(@PathVariable UUID id) {
        return escrowTransactionRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<EscrowTransaction>> getTransactions(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String workerId) {
        
        List<EscrowTransaction> transactions;
        if (tenantId != null) {
            transactions = escrowTransactionRepository.findByTenantId(TenantId.of(tenantId));
        } else if (clientId != null) {
            transactions = escrowTransactionRepository.findByClientId(clientId);
        } else if (workerId != null) {
            transactions = escrowTransactionRepository.findByWorkerId(workerId);
        } else {
            transactions = escrowTransactionRepository.findAll();
        }
        
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/transactions/{id}/complete")
    public ResponseEntity<EscrowTransaction> completeTransaction(@PathVariable UUID id) {
        log.info("Completing transaction: {}", id);
        
        EscrowTransaction transaction = escrowTransactionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        transaction.complete();
        EscrowTransaction savedTransaction = escrowTransactionRepository.save(transaction);
        
        return ResponseEntity.ok(savedTransaction);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Escrow Service");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("endpoints", Map.of(
            "health", "/api/escrow/health",
            "accounts", "/api/escrow/accounts",
            "transactions", "/api/escrow/transactions",
            "createAccount", "POST /api/escrow/accounts",
            "deposit", "POST /api/escrow/accounts/{id}/deposit"
        ));
        response.put("documentation", "See README.md for API documentation");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugEndpoint(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Test Money creation
            Money money = new Money(java.math.BigDecimal.ZERO, java.util.Currency.getInstance("USD"));
            response.put("money_created", money.toString());
            
            // Test AccountType
            EscrowAccount.AccountType accountType = EscrowAccount.AccountType.valueOf("CLIENT");
            response.put("account_type", accountType.name());
            
            // Test UUID
            UUID accountHolderId = UUID.fromString("client-001");
            response.put("uuid_created", accountHolderId.toString());
            
            // Test EscrowAccount creation
            EscrowAccount account = new EscrowAccount(
                "tenant-001",
                accountHolderId,
                accountType,
                money
            );
            response.put("account_created", "SUCCESS");
            response.put("account_id", account.getId());
            
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("error_class", e.getClass().getSimpleName());
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Escrow Service is running");
    }
}
