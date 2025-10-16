package com.microjobs.escrow.web;

import com.microjobs.escrow.domain.EscrowAccount;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class CreateAccountRequest {
    
    @NotBlank(message = "Tenant ID is required")
    private String tenantId;
    
    @NotBlank(message = "Account holder ID is required")
    private String accountHolderId;
    
    @NotNull(message = "Account type is required")
    private EscrowAccount.AccountType accountType;
}
