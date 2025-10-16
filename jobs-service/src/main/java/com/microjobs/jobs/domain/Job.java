package com.microjobs.jobs.domain;

import com.microjobs.shared.domain.AggregateRoot;
import com.microjobs.shared.domain.Money;
import com.microjobs.shared.domain.TenantId;
import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jobs", schema = "jobs")
@Getter
@Setter
@NoArgsConstructor
public class Job extends AggregateRoot {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private JobStatus status;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "budget_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "budget_currency"))
    })
    private Money budget;
    
    @Column(name = "deadline")
    private LocalDateTime deadline;
    
    @Column(name = "required_skills", columnDefinition = "TEXT[]")
    @ElementCollection
    private List<String> requiredSkills = new ArrayList<>();
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "max_distance_km")
    private Double maxDistanceKm;
    
    @Column(name = "client_id", nullable = false)
    private UUID clientId;
    
    @Column(name = "assigned_worker_id")
    private UUID assignedWorkerId;
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JobBid> bids = new ArrayList<>();
    
    public Job(TenantId tenantId, String title, String description, Money budget, 
               LocalDateTime deadline, List<String> requiredSkills, String location,
               Double latitude, Double longitude, Double maxDistanceKm, UUID clientId) {
        super(tenantId.toString());
        this.status = JobStatus.OPEN;
        this.title = title;
        this.description = description;
        this.budget = budget;
        this.deadline = deadline;
        this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>();
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxDistanceKm = maxDistanceKm;
        this.clientId = clientId;
    }
    
    public void assignToWorker(UUID workerId) {
        if (status != JobStatus.OPEN) {
            throw new IllegalStateException("Job must be open to assign to worker");
        }
        this.assignedWorkerId = workerId;
        this.assignedAt = LocalDateTime.now();
        this.status = JobStatus.ASSIGNED;
    }
    
    public void markAsCompleted() {
        if (status != JobStatus.ASSIGNED) {
            throw new IllegalStateException("Job must be assigned to mark as completed");
        }
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void cancel() {
        if (status == JobStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed job");
        }
        this.status = JobStatus.CANCELLED;
    }
    
    public boolean isWithinDistance(Double workerLatitude, Double workerLongitude) {
        if (latitude == null || longitude == null || workerLatitude == null || workerLongitude == null) {
            return true; // No location constraint
        }
        if (maxDistanceKm == null) {
            return true; // No distance constraint
        }
        
        double distance = calculateDistance(latitude, longitude, workerLatitude, workerLongitude);
        return distance <= maxDistanceKm;
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    @Override
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Job title is required");
        }
        if (budget == null || budget.isZero()) {
            throw new IllegalArgumentException("Job budget must be greater than zero");
        }
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID is required");
        }
        if (deadline != null && deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }
    }
    
    public enum JobStatus {
        OPEN, ASSIGNED, COMPLETED, CANCELLED
    }
}
