package com.microjobs.search.ports;

import com.microjobs.search.domain.JobSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobSearchRepository extends ElasticsearchRepository<JobSearchDocument, String> {

    // Full-text search
    Page<JobSearchDocument> findByTitleContainingOrDescriptionContaining(
            String title, String description, Pageable pageable);

    // Search by skills
    Page<JobSearchDocument> findByRequiredSkillsIn(List<String> skills, Pageable pageable);

    // Search by location and distance
    @Query("{\"bool\": {\"must\": [{\"match\": {\"location\": \"?0\"}}], \"filter\": [{\"geo_distance\": {\"distance\": \"?1km\", \"geoLocation\": {\"lat\": ?2, \"lon\": ?3}}}]}}")
    Page<JobSearchDocument> findByLocationNear(String location, Double distance, 
                                              Double latitude, Double longitude, Pageable pageable);

    // Search by budget range
    Page<JobSearchDocument> findByBudgetAmountBetween(BigDecimal minBudget, BigDecimal maxBudget, Pageable pageable);

    // Search by status
    Page<JobSearchDocument> findByStatus(String status, Pageable pageable);

    // Search by tenant
    Page<JobSearchDocument> findByTenantId(String tenantId, Pageable pageable);

    // Search by client
    Page<JobSearchDocument> findByClientId(String clientId, Pageable pageable);

    // Search by deadline
    Page<JobSearchDocument> findByDeadlineBefore(LocalDateTime deadline, Pageable pageable);

    // Search by multiple criteria
    @Query("{\"bool\": {\"must\": [{\"match\": {\"title\": \"?0\"}}, {\"terms\": {\"requiredSkills\": ?1}}, {\"range\": {\"budgetAmount\": {\"gte\": ?2, \"lte\": ?3}}}]}}")
    Page<JobSearchDocument> findByTitleAndSkillsAndBudgetRange(String title, List<String> skills,
                                                             BigDecimal minBudget, BigDecimal maxBudget, Pageable pageable);

    // Search urgent jobs
    Page<JobSearchDocument> findByIsUrgentTrue(Pageable pageable);

    // Search remote jobs
    Page<JobSearchDocument> findByIsRemoteTrue(Pageable pageable);

    // Search by experience level
    Page<JobSearchDocument> findByExperienceLevelLessThanEqual(Integer maxExperience, Pageable pageable);

    // Complex search with multiple filters
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^2\", \"description\", \"requiredSkills\"]}}, {\"terms\": {\"status\": ?1}}, {\"range\": {\"budgetAmount\": {\"gte\": ?2}}}, {\"geo_distance\": {\"distance\": \"?3km\", \"geoLocation\": {\"lat\": ?4, \"lon\": ?5}}}]}}")
    Page<JobSearchDocument> searchJobsWithFilters(String query, List<String> statuses, BigDecimal minBudget,
                                                 Double maxDistance, Double latitude, Double longitude, Pageable pageable);
}
