package com.microjobs.analytics.ports;

import com.microjobs.analytics.domain.JobMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface JobMetricsRepository extends JpaRepository<JobMetrics, UUID> {

    List<JobMetrics> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    Page<JobMetrics> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<JobMetrics> findByTenantIdAndStatus(String tenantId, String status);

    List<JobMetrics> findByTenantIdAndCategory(String tenantId, String category);

    List<JobMetrics> findByTenantIdAndClientId(String tenantId, UUID clientId);

    List<JobMetrics> findByTenantIdAndWorkerId(String tenantId, UUID workerId);

    @Query("SELECT j FROM JobMetrics j WHERE j.tenantId = :tenantId AND j.createdAt >= :startDate AND j.createdAt <= :endDate")
    List<JobMetrics> findByTenantIdAndDateRange(@Param("tenantId") String tenantId, 
                                               @Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(j) FROM JobMetrics j WHERE j.tenantId = :tenantId AND j.status = :status")
    Long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    @Query("SELECT AVG(j.budgetAmount) FROM JobMetrics j WHERE j.tenantId = :tenantId AND j.status = 'COMPLETED'")
    BigDecimal getAverageJobValueByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT SUM(j.budgetAmount) FROM JobMetrics j WHERE j.tenantId = :tenantId AND j.status = 'COMPLETED'")
    BigDecimal getTotalJobValueByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT j.category, COUNT(j) FROM JobMetrics j WHERE j.tenantId = :tenantId GROUP BY j.category")
    List<Object[]> getJobCountByCategory(@Param("tenantId") String tenantId);

    @Query("SELECT j.location, COUNT(j) FROM JobMetrics j WHERE j.tenantId = :tenantId AND j.location IS NOT NULL GROUP BY j.location")
    List<Object[]> getJobCountByLocation(@Param("tenantId") String tenantId);

    @Query("SELECT AVG(j.completionTimeHours) FROM JobMetrics j WHERE j.tenantId = :tenantId AND j.completionTimeHours IS NOT NULL")
    Double getAverageCompletionTimeByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT AVG(j.clientRating) FROM JobMetrics j WHERE j.tenantId = :tenantId AND j.clientRating IS NOT NULL")
    Double getAverageClientRatingByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT AVG(j.workerRating) FROM JobMetrics j WHERE j.tenantId = :tenantId AND j.workerRating IS NOT NULL")
    Double getAverageWorkerRatingByTenant(@Param("tenantId") String tenantId);

    JobMetrics findByTenantIdAndJobId(String tenantId, UUID jobId);
}
