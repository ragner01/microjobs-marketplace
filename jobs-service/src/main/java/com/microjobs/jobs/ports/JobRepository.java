package com.microjobs.jobs.ports;

import com.microjobs.jobs.domain.Job;
import com.microjobs.jobs.domain.JobStatus;
import com.microjobs.shared.domain.TenantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {
    
    List<Job> findByTenantId(TenantId tenantId);
    
    List<Job> findByStatus(JobStatus status);
    
    List<Job> findByClientId(String clientId);
    
    List<Job> findByAssignedWorkerId(String workerId);
}
