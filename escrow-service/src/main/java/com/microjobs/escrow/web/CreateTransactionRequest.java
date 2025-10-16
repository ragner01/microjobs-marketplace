package com.microjobs.escrow.web;

import com.microjobs.escrow.domain.EscrowTransaction;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateTransactionRequest {
    
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    
    @NotNull(message = "Job ID is required")
    private UUID jobId;
    
    @NotBlank(message = "Client ID is required")
    private String clientId;
    
    @NotBlank(message = "Worker ID is required")
    private String workerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Transaction type is required")
    private EscrowTransaction.TransactionType type;
    
    private String description;
}
