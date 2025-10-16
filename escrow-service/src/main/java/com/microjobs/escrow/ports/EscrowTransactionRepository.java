package com.microjobs.escrow.ports;

import com.microjobs.escrow.domain.EscrowTransaction;
import com.microjobs.shared.domain.TenantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EscrowTransactionRepository extends JpaRepository<EscrowTransaction, UUID> {
    
    List<EscrowTransaction> findByTenantId(TenantId tenantId);
    
    List<EscrowTransaction> findByClientId(String clientId);
    
    List<EscrowTransaction> findByWorkerId(String workerId);
    
    List<EscrowTransaction> findByJobId(UUID jobId);
}