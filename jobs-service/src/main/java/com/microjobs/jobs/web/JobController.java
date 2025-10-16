package com.microjobs.jobs.web;

import com.microjobs.jobs.domain.Job;
import com.microjobs.jobs.domain.JobBid;
import com.microjobs.jobs.domain.JobStatus;
import com.microjobs.jobs.domain.JobBidStatus;
import com.microjobs.jobs.ports.JobRepository;
import com.microjobs.jobs.ports.JobBidRepository;
import com.microjobs.shared.domain.Money;
import com.microjobs.shared.domain.TenantId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobController {

    private final JobRepository jobRepository;
    private final JobBidRepository jobBidRepository;

    @PostMapping
    public ResponseEntity<Job> createJob(@Valid @RequestBody CreateJobRequest request) {
        log.info("Creating job: {}", request.getTitle());
        
        Job job = new Job(
            TenantId.of(request.getTenantId()),
            request.getTitle(),
            request.getDescription(),
            new Money(request.getBudgetAmount(), request.getBudgetCurrency()),
            request.getDeadline(),
            request.getRequiredSkills(),
            request.getLocation(),
            request.getLatitude(),
            request.getLongitude(),
            request.getMaxDistanceKm() != null ? request.getMaxDistanceKm().doubleValue() : null,
            UUID.randomUUID() // Generate UUID for client ID
        );
        
        Job savedJob = jobRepository.save(job);
        return ResponseEntity.ok(savedJob);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJob(@PathVariable UUID id) {
        return jobRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Job>> getJobs(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) String clientId) {
        
        List<Job> jobs;
        if (tenantId != null) {
            jobs = jobRepository.findByTenantId(TenantId.of(tenantId));
        } else if (status != null) {
            jobs = jobRepository.findByStatus(status);
        } else if (clientId != null) {
            jobs = jobRepository.findByClientId(clientId);
        } else {
            jobs = jobRepository.findAll();
        }
        
        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/{jobId}/bids")
    public ResponseEntity<JobBid> submitBid(@PathVariable UUID jobId, @Valid @RequestBody SubmitBidRequest request) {
        log.info("Submitting bid for job: {}", jobId);
        
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        
        JobBid bid = new JobBid(
            job,
            UUID.fromString(request.getWorkerId()),
            new Money(request.getBidAmount(), request.getBidCurrency()),
            request.getProposal(),
            request.getEstimatedCompletionDays()
        );
        
        JobBid savedBid = jobBidRepository.save(bid);
        return ResponseEntity.ok(savedBid);
    }

    @GetMapping("/{jobId}/bids")
    public ResponseEntity<List<JobBid>> getJobBids(@PathVariable UUID jobId) {
        List<JobBid> bids = jobBidRepository.findByJobId(jobId);
        return ResponseEntity.ok(bids);
    }

    @PutMapping("/{jobId}/assign/{bidId}")
    public ResponseEntity<Job> assignJob(@PathVariable UUID jobId, @PathVariable UUID bidId) {
        log.info("Assigning job {} to bid {}", jobId, bidId);
        
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        
        JobBid bid = jobBidRepository.findById(bidId)
            .orElseThrow(() -> new IllegalArgumentException("Bid not found"));
        
        job.assignToWorker(UUID.fromString(bid.getWorkerId().toString()));
        Job savedJob = jobRepository.save(job);
        
        return ResponseEntity.ok(savedJob);
    }

    @PutMapping("/{jobId}/complete")
    public ResponseEntity<Job> completeJob(@PathVariable UUID jobId) {
        log.info("Completing job: {}", jobId);
        
        Job job = jobRepository.findById(jobId)
            .orElseThrow(() -> new IllegalArgumentException("Job not found"));
        
        job.markAsCompleted();
        Job savedJob = jobRepository.save(job);
        
        return ResponseEntity.ok(savedJob);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Jobs Service");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("endpoints", Map.of(
            "health", "/api/jobs/health",
            "jobs", "/api/jobs",
            "createJob", "POST /api/jobs",
            "getJob", "GET /api/jobs/{id}",
            "submitBid", "POST /api/jobs/{jobId}/bids"
        ));
        response.put("documentation", "See README.md for API documentation");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Jobs Service is running");
    }
}
