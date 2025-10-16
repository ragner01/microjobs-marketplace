package com.microjobs.analytics.ports;

import com.microjobs.analytics.domain.PlatformMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlatformMetricsRepository extends JpaRepository<PlatformMetrics, UUID> {

    List<PlatformMetrics> findByTenantIdOrderByMetricDateDesc(String tenantId);

    PlatformMetrics findByTenantIdAndMetricDate(String tenantId, LocalDateTime metricDate);

    @Query("SELECT p FROM PlatformMetrics p WHERE p.tenantId = :tenantId AND p.metricDate >= :startDate AND p.metricDate <= :endDate ORDER BY p.metricDate DESC")
    List<PlatformMetrics> findByTenantIdAndDateRange(@Param("tenantId") String tenantId, 
                                                    @Param("startDate") LocalDateTime startDate, 
                                                    @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p FROM PlatformMetrics p WHERE p.tenantId = :tenantId ORDER BY p.metricDate DESC")
    List<PlatformMetrics> findLatestByTenantId(@Param("tenantId") String tenantId);

    @Query("SELECT AVG(p.jobCompletionRate) FROM PlatformMetrics p WHERE p.tenantId = :tenantId AND p.metricDate >= :startDate")
    Double getAverageJobCompletionRate(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT AVG(p.userSatisfactionScore) FROM PlatformMetrics p WHERE p.tenantId = :tenantId AND p.metricDate >= :startDate")
    Double getAverageUserSatisfactionScore(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(p.revenueToday) FROM PlatformMetrics p WHERE p.tenantId = :tenantId AND p.metricDate >= :startDate")
    java.math.BigDecimal getTotalRevenueByDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(p.newUsersToday) FROM PlatformMetrics p WHERE p.tenantId = :tenantId AND p.metricDate >= :startDate")
    Long getTotalNewUsersByDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT SUM(p.newJobsToday) FROM PlatformMetrics p WHERE p.tenantId = :tenantId AND p.metricDate >= :startDate")
    Long getTotalNewJobsByDateRange(@Param("tenantId") String tenantId, @Param("startDate") LocalDateTime startDate);
}
