package com.microjobs.analytics.ports;

import com.microjobs.analytics.domain.UserMetrics;
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
public interface UserMetricsRepository extends JpaRepository<UserMetrics, UUID> {

    List<UserMetrics> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    Page<UserMetrics> findByTenantIdOrderByCreatedAtDesc(String tenantId, Pageable pageable);

    List<UserMetrics> findByTenantIdAndUserType(String tenantId, String userType);

    List<UserMetrics> findByTenantIdAndStatus(String tenantId, String status);

    UserMetrics findByTenantIdAndUserId(String tenantId, UUID userId);

    @Query("SELECT u FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.lastActiveAt >= :activeSince")
    List<UserMetrics> findActiveUsersByTenant(@Param("tenantId") String tenantId, 
                                            @Param("activeSince") LocalDateTime activeSince);

    @Query("SELECT COUNT(u) FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.userType = :userType")
    Long countByTenantIdAndUserType(@Param("tenantId") String tenantId, @Param("userType") String userType);

    @Query("SELECT COUNT(u) FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.status = :status")
    Long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") String status);

    @Query("SELECT AVG(u.averageRating) FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.averageRating > 0")
    Double getAverageRatingByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT SUM(u.totalEarnings) FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.userType = 'WORKER'")
    BigDecimal getTotalEarningsByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT SUM(u.totalSpent) FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.userType = 'CLIENT'")
    BigDecimal getTotalSpentByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT u.location, COUNT(u) FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.location IS NOT NULL GROUP BY u.location")
    List<Object[]> getUserCountByLocation(@Param("tenantId") String tenantId);

    @Query("SELECT AVG(u.completionRate) FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.userType = 'WORKER' AND u.completionRate > 0")
    Double getAverageCompletionRateByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT AVG(u.responseTimeHours) FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.responseTimeHours > 0")
    Double getAverageResponseTimeByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT COUNT(u) FROM UserMetrics u WHERE u.tenantId = :tenantId AND u.createdAt >= :startDate AND u.createdAt <= :endDate")
    Long countNewUsersByDateRange(@Param("tenantId") String tenantId, 
                                 @Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
}
