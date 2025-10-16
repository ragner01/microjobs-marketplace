package com.microjobs.analytics.service;

import com.microjobs.analytics.domain.JobMetrics;
import com.microjobs.analytics.domain.PlatformMetrics;
import com.microjobs.analytics.domain.UserMetrics;
import com.microjobs.analytics.ports.JobMetricsRepository;
import com.microjobs.analytics.ports.PlatformMetricsRepository;
import com.microjobs.analytics.ports.UserMetricsRepository;
import com.microjobs.shared.infrastructure.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AnalyticsService {

    private final JobMetricsRepository jobMetricsRepository;
    private final UserMetricsRepository userMetricsRepository;
    private final PlatformMetricsRepository platformMetricsRepository;
    private final SecurityContextHelper securityContextHelper;

    // Job Analytics
    public Map<String, Object> getJobAnalytics(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating job analytics for tenant: {} from {} to {}", tenantId, startDate, endDate);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Basic counts
        Long totalJobs = jobMetricsRepository.countByTenantIdAndStatus(tenantId, "OPEN") +
                        jobMetricsRepository.countByTenantIdAndStatus(tenantId, "ASSIGNED") +
                        jobMetricsRepository.countByTenantIdAndStatus(tenantId, "COMPLETED");
        Long activeJobs = jobMetricsRepository.countByTenantIdAndStatus(tenantId, "OPEN") +
                         jobMetricsRepository.countByTenantIdAndStatus(tenantId, "ASSIGNED");
        Long completedJobs = jobMetricsRepository.countByTenantIdAndStatus(tenantId, "COMPLETED");
        
        analytics.put("totalJobs", totalJobs);
        analytics.put("activeJobs", activeJobs);
        analytics.put("completedJobs", completedJobs);
        analytics.put("completionRate", totalJobs > 0 ? (double) completedJobs / totalJobs : 0.0);
        
        // Financial metrics
        BigDecimal totalValue = jobMetricsRepository.getTotalJobValueByTenant(tenantId);
        BigDecimal averageValue = jobMetricsRepository.getAverageJobValueByTenant(tenantId);
        
        analytics.put("totalJobValue", totalValue != null ? totalValue : BigDecimal.ZERO);
        analytics.put("averageJobValue", averageValue != null ? averageValue : BigDecimal.ZERO);
        
        // Performance metrics
        Double averageCompletionTime = jobMetricsRepository.getAverageCompletionTimeByTenant(tenantId);
        Double averageClientRating = jobMetricsRepository.getAverageClientRatingByTenant(tenantId);
        Double averageWorkerRating = jobMetricsRepository.getAverageWorkerRatingByTenant(tenantId);
        
        analytics.put("averageCompletionTimeHours", averageCompletionTime != null ? averageCompletionTime : 0.0);
        analytics.put("averageClientRating", averageClientRating != null ? averageClientRating : 0.0);
        analytics.put("averageWorkerRating", averageWorkerRating != null ? averageWorkerRating : 0.0);
        
        // Category breakdown
        List<Object[]> categoryData = jobMetricsRepository.getJobCountByCategory(tenantId);
        Map<String, Long> categoryBreakdown = new HashMap<>();
        for (Object[] row : categoryData) {
            categoryBreakdown.put((String) row[0], (Long) row[1]);
        }
        analytics.put("categoryBreakdown", categoryBreakdown);
        
        // Location breakdown
        List<Object[]> locationData = jobMetricsRepository.getJobCountByLocation(tenantId);
        Map<String, Long> locationBreakdown = new HashMap<>();
        for (Object[] row : locationData) {
            locationBreakdown.put((String) row[0], (Long) row[1]);
        }
        analytics.put("locationBreakdown", locationBreakdown);
        
        return analytics;
    }

    // User Analytics
    public Map<String, Object> getUserAnalytics(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating user analytics for tenant: {} from {} to {}", tenantId, startDate, endDate);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // User counts
        Long totalUsers = userMetricsRepository.countByTenantIdAndUserType(tenantId, "CLIENT") +
                         userMetricsRepository.countByTenantIdAndUserType(tenantId, "WORKER");
        Long totalClients = userMetricsRepository.countByTenantIdAndUserType(tenantId, "CLIENT");
        Long totalWorkers = userMetricsRepository.countByTenantIdAndUserType(tenantId, "WORKER");
        Long activeUsers = (long) userMetricsRepository.findActiveUsersByTenant(tenantId, 
            LocalDateTime.now().minus(7, ChronoUnit.DAYS)).size();
        
        analytics.put("totalUsers", totalUsers);
        analytics.put("totalClients", totalClients);
        analytics.put("totalWorkers", totalWorkers);
        analytics.put("activeUsers", activeUsers);
        analytics.put("userGrowthRate", calculateGrowthRate(tenantId, startDate, endDate));
        
        // Financial metrics
        BigDecimal totalEarnings = userMetricsRepository.getTotalEarningsByTenant(tenantId);
        BigDecimal totalSpent = userMetricsRepository.getTotalSpentByTenant(tenantId);
        
        analytics.put("totalEarnings", totalEarnings != null ? totalEarnings : BigDecimal.ZERO);
        analytics.put("totalSpent", totalSpent != null ? totalSpent : BigDecimal.ZERO);
        
        // Performance metrics
        Double averageRating = userMetricsRepository.getAverageRatingByTenant(tenantId);
        Double averageCompletionRate = userMetricsRepository.getAverageCompletionRateByTenant(tenantId);
        Double averageResponseTime = userMetricsRepository.getAverageResponseTimeByTenant(tenantId);
        
        analytics.put("averageRating", averageRating != null ? averageRating : 0.0);
        analytics.put("averageCompletionRate", averageCompletionRate != null ? averageCompletionRate : 0.0);
        analytics.put("averageResponseTimeHours", averageResponseTime != null ? averageResponseTime : 0.0);
        
        // Location breakdown
        List<Object[]> locationData = userMetricsRepository.getUserCountByLocation(tenantId);
        Map<String, Long> locationBreakdown = new HashMap<>();
        for (Object[] row : locationData) {
            locationBreakdown.put((String) row[0], (Long) row[1]);
        }
        analytics.put("locationBreakdown", locationBreakdown);
        
        return analytics;
    }

    // Platform Analytics
    public Map<String, Object> getPlatformAnalytics(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Generating platform analytics for tenant: {} from {} to {}", tenantId, startDate, endDate);
        
        Map<String, Object> analytics = new HashMap<>();
        
        // Get latest platform metrics
        List<PlatformMetrics> latestMetricsList = platformMetricsRepository.findLatestByTenantId(tenantId);
        PlatformMetrics latestMetrics = latestMetricsList.isEmpty() ? null : latestMetricsList.get(0);
        if (latestMetrics != null) {
            analytics.put("totalUsers", latestMetrics.getTotalUsers());
            analytics.put("activeUsers", latestMetrics.getActiveUsers());
            analytics.put("totalJobs", latestMetrics.getTotalJobs());
            analytics.put("activeJobs", latestMetrics.getActiveJobs());
            analytics.put("completedJobs", latestMetrics.getCompletedJobs());
            analytics.put("totalVolume", latestMetrics.getTotalVolume());
            analytics.put("averageJobValue", latestMetrics.getAverageJobValue());
            analytics.put("platformFee", latestMetrics.getPlatformFee());
            analytics.put("jobCompletionRate", latestMetrics.getJobCompletionRate());
            analytics.put("userSatisfactionScore", latestMetrics.getUserSatisfactionScore());
        }
        
        // Growth metrics
        analytics.put("newUsersToday", latestMetrics != null ? latestMetrics.getNewUsersToday() : 0);
        analytics.put("newJobsToday", latestMetrics != null ? latestMetrics.getNewJobsToday() : 0);
        analytics.put("revenueToday", latestMetrics != null ? latestMetrics.getRevenueToday() : BigDecimal.ZERO);
        
        // Historical trends
        List<PlatformMetrics> historicalData = platformMetricsRepository.findByTenantIdAndDateRange(tenantId, startDate, endDate);
        analytics.put("historicalData", historicalData);
        
        return analytics;
    }

    // KPI Dashboard
    public Map<String, Object> getKPIDashboard(String tenantId) {
        log.info("Generating KPI dashboard for tenant: {}", tenantId);
        
        Map<String, Object> kpis = new HashMap<>();
        
        // Job KPIs
        Map<String, Object> jobAnalytics = getJobAnalytics(tenantId, 
            LocalDateTime.now().minus(30, ChronoUnit.DAYS), LocalDateTime.now());
        kpis.put("jobKPIs", jobAnalytics);
        
        // User KPIs
        Map<String, Object> userAnalytics = getUserAnalytics(tenantId, 
            LocalDateTime.now().minus(30, ChronoUnit.DAYS), LocalDateTime.now());
        kpis.put("userKPIs", userAnalytics);
        
        // Platform KPIs
        Map<String, Object> platformAnalytics = getPlatformAnalytics(tenantId, 
            LocalDateTime.now().minus(30, ChronoUnit.DAYS), LocalDateTime.now());
        kpis.put("platformKPIs", platformAnalytics);
        
        // Calculate overall health score
        double healthScore = calculatePlatformHealthScore(jobAnalytics, userAnalytics, platformAnalytics);
        kpis.put("platformHealthScore", healthScore);
        
        return kpis;
    }

    // Data Management Methods
    public void recordJobCreated(UUID jobId, UUID clientId, String title, String category, 
                               BigDecimal budgetAmount, String budgetCurrency, String location) {
        String tenantId = securityContextHelper.getCurrentTenantId();
        JobMetrics metrics = new JobMetrics(tenantId, jobId, clientId, title, category, 
                                          budgetAmount, budgetCurrency, "OPEN");
        metrics.setLocation(location);
        jobMetricsRepository.save(metrics);
        log.info("Recorded job creation metrics for job: {}", jobId);
    }

    public void recordJobStatusChange(UUID jobId, String newStatus) {
        String tenantId = securityContextHelper.getCurrentTenantId();
        JobMetrics metrics = jobMetricsRepository.findByTenantIdAndJobId(tenantId, jobId);
        if (metrics != null) {
            metrics.updateStatus(newStatus);
            jobMetricsRepository.save(metrics);
            log.info("Updated job status metrics for job: {} to {}", jobId, newStatus);
        }
    }

    public void recordUserActivity(UUID userId, String userType, String email) {
        String tenantId = securityContextHelper.getCurrentTenantId();
        UserMetrics metrics = userMetricsRepository.findByTenantIdAndUserId(tenantId, userId);
        if (metrics == null) {
            metrics = new UserMetrics(tenantId, userId, email, userType, "ACTIVE");
            userMetricsRepository.save(metrics);
            log.info("Created user metrics for user: {}", userId);
        } else {
            metrics.updateLastActive();
            userMetricsRepository.save(metrics);
            log.info("Updated user activity for user: {}", userId);
        }
    }

    private double calculateGrowthRate(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        Long newUsers = userMetricsRepository.countNewUsersByDateRange(tenantId, startDate, endDate);
        Long totalUsers = userMetricsRepository.countByTenantIdAndUserType(tenantId, "CLIENT") +
                         userMetricsRepository.countByTenantIdAndUserType(tenantId, "WORKER");
        
        if (totalUsers == 0) return 0.0;
        return (double) newUsers / totalUsers * 100;
    }

    private double calculatePlatformHealthScore(Map<String, Object> jobAnalytics, 
                                              Map<String, Object> userAnalytics, 
                                              Map<String, Object> platformAnalytics) {
        // Simple health score calculation based on key metrics
        double completionRate = (Double) jobAnalytics.getOrDefault("completionRate", 0.0);
        double averageRating = (Double) userAnalytics.getOrDefault("averageRating", 0.0);
        double userSatisfaction = (Double) platformAnalytics.getOrDefault("userSatisfactionScore", 0.0);
        
        return (completionRate * 0.4 + averageRating * 0.3 + userSatisfaction * 0.3) * 100;
    }
}
