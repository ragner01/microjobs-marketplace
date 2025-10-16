package com.microjobs.escrow.ports;

import com.microjobs.escrow.domain.EscrowAccount;
import com.microjobs.shared.domain.TenantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EscrowAccountRepository extends JpaRepository<EscrowAccount, UUID> {
    
    List<EscrowAccount> findByTenantId(String tenantId);
    
    List<EscrowAccount> findByAccountHolderId(String accountHolderId);
    
    Optional<EscrowAccount> findByAccountHolderIdAndAccountType(UUID accountHolderId, EscrowAccount.AccountType accountType);
}