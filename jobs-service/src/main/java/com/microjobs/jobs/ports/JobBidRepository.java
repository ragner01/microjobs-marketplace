package com.microjobs.jobs.ports;

import com.microjobs.jobs.domain.JobBid;
import com.microjobs.jobs.domain.JobBidStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JobBidRepository extends JpaRepository<JobBid, UUID> {
    
    List<JobBid> findByJobId(UUID jobId);
    
    List<JobBid> findByWorkerId(String workerId);
    
    List<JobBid> findByStatus(JobBidStatus status);
}
