package com.microjobs.search.service;

import com.microjobs.search.domain.JobSearchDocument;
import com.microjobs.search.domain.UserSearchDocument;
import com.microjobs.search.ports.JobSearchRepository;
import com.microjobs.search.ports.UserSearchRepository;
import com.microjobs.shared.infrastructure.security.SecurityContextHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SearchService {

    private final JobSearchRepository jobSearchRepository;
    private final UserSearchRepository userSearchRepository;
    private final ElasticsearchRestTemplate elasticsearchTemplate;
    private final SecurityContextHelper securityContextHelper;

    // Job Search Methods
    public Page<JobSearchDocument> searchJobs(String query, List<String> skills, BigDecimal minBudget, 
                                            BigDecimal maxBudget, String location, Double latitude, 
                                            Double longitude, Double maxDistance, List<String> statuses,
                                            Boolean isUrgent, Boolean isRemote, Integer maxExperience,
                                            int page, int size, String sortBy, String sortDirection) {
        
        log.info("Searching jobs with query: {}, skills: {}, budget: {}-{}, location: {}", 
                query, skills, minBudget, maxBudget, location);

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        String tenantId = securityContextHelper.getCurrentTenantId();

        // Build complex query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // Tenant filter
        boolQuery.must(QueryBuilders.termQuery("tenantId", tenantId));

        // Text search
        if (query != null && !query.trim().isEmpty()) {
            boolQuery.must(QueryBuilders.multiMatchQuery(query)
                    .field("title", 2.0f)  // Boost title matches
                    .field("description")
                    .field("requiredSkills")
                    .type(org.elasticsearch.index.query.MultiMatchQueryBuilder.Type.BEST_FIELDS));
        }

        // Skills filter
        if (skills != null && !skills.isEmpty()) {
            boolQuery.must(QueryBuilders.termsQuery("requiredSkills", skills));
        }

        // Budget range filter
        if (minBudget != null || maxBudget != null) {
            org.elasticsearch.index.query.RangeQueryBuilder budgetQuery = QueryBuilders.rangeQuery("budgetAmount");
            if (minBudget != null) budgetQuery.gte(minBudget);
            if (maxBudget != null) budgetQuery.lte(maxBudget);
            boolQuery.must(budgetQuery);
        }

        // Location filter
        if (latitude != null && longitude != null && maxDistance != null) {
            boolQuery.filter(QueryBuilders.geoDistanceQuery("geoLocation")
                    .point(latitude, longitude)
                    .distance(maxDistance + "km"));
        }

        // Status filter
        if (statuses != null && !statuses.isEmpty()) {
            boolQuery.must(QueryBuilders.termsQuery("status", statuses));
        }

        // Urgent filter
        if (isUrgent != null) {
            boolQuery.must(QueryBuilders.termQuery("isUrgent", isUrgent));
        }

        // Remote filter
        if (isRemote != null) {
            boolQuery.must(QueryBuilders.termQuery("isRemote", isRemote));
        }

        // Experience level filter
        if (maxExperience != null) {
            boolQuery.must(QueryBuilders.rangeQuery("experienceLevel").lte(maxExperience));
        }

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        SearchHits<JobSearchDocument> searchHits = elasticsearchTemplate.search(searchQuery, JobSearchDocument.class);
        
        List<JobSearchDocument> jobs = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(jobs, pageable, searchHits.getTotalHits());
    }

    public Page<JobSearchDocument> searchJobsByLocation(String location, Double latitude, Double longitude, 
                                                      Double maxDistance, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobSearchRepository.findByLocationNear(location, maxDistance, latitude, longitude, pageable);
    }

    public Page<JobSearchDocument> searchJobsBySkills(List<String> skills, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobSearchRepository.findByRequiredSkillsIn(skills, pageable);
    }

    public Page<JobSearchDocument> searchJobsByBudget(BigDecimal minBudget, BigDecimal maxBudget, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return jobSearchRepository.findByBudgetAmountBetween(minBudget, maxBudget, pageable);
    }

    // User Search Methods
    public Page<UserSearchDocument> searchUsers(String query, List<String> skills, String userType,
                                              Double minRate, Double maxRate, String location,
                                              Double latitude, Double longitude, Double maxDistance,
                                              Integer minExperience, Double minRating, Boolean isAvailable,
                                              int page, int size, String sortBy, String sortDirection) {
        
        log.info("Searching users with query: {}, skills: {}, userType: {}", query, skills, userType);

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        String tenantId = securityContextHelper.getCurrentTenantId();

        // Build complex query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // Tenant filter
        boolQuery.must(QueryBuilders.termQuery("tenantId", tenantId));

        // Text search
        if (query != null && !query.trim().isEmpty()) {
            boolQuery.must(QueryBuilders.multiMatchQuery(query)
                    .field("firstName", 2.0f)  // Boost name matches
                    .field("lastName", 2.0f)
                    .field("bio")
                    .field("skills")
                    .type(org.elasticsearch.index.query.MultiMatchQueryBuilder.Type.BEST_FIELDS));
        }

        // Skills filter
        if (skills != null && !skills.isEmpty()) {
            boolQuery.must(QueryBuilders.termsQuery("skills", skills));
        }

        // User type filter
        if (userType != null && !userType.trim().isEmpty()) {
            boolQuery.must(QueryBuilders.termQuery("userType", userType));
        }

        // Rate range filter
        if (minRate != null || maxRate != null) {
            org.elasticsearch.index.query.RangeQueryBuilder rateQuery = QueryBuilders.rangeQuery("hourlyRate");
            if (minRate != null) rateQuery.gte(minRate);
            if (maxRate != null) rateQuery.lte(maxRate);
            boolQuery.must(rateQuery);
        }

        // Location filter
        if (latitude != null && longitude != null && maxDistance != null) {
            boolQuery.filter(QueryBuilders.geoDistanceQuery("geoLocation")
                    .point(latitude, longitude)
                    .distance(maxDistance + "km"));
        }

        // Experience filter
        if (minExperience != null) {
            boolQuery.must(QueryBuilders.rangeQuery("experienceYears").gte(minExperience));
        }

        // Rating filter
        if (minRating != null) {
            boolQuery.must(QueryBuilders.rangeQuery("rating").gte(minRating));
        }

        // Availability filter
        if (isAvailable != null) {
            boolQuery.must(QueryBuilders.termQuery("isAvailable", isAvailable));
        }

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        SearchHits<UserSearchDocument> searchHits = elasticsearchTemplate.search(searchQuery, UserSearchDocument.class);
        
        List<UserSearchDocument> users = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(users, pageable, searchHits.getTotalHits());
    }

    // Index Management Methods
    public void indexJob(JobSearchDocument job) {
        log.info("Indexing job: {}", job.getId());
        jobSearchRepository.save(job);
    }

    public void indexUser(UserSearchDocument user) {
        log.info("Indexing user: {}", user.getId());
        userSearchRepository.save(user);
    }

    public void deleteJob(String jobId) {
        log.info("Deleting job from index: {}", jobId);
        jobSearchRepository.deleteById(jobId);
    }

    public void deleteUser(String userId) {
        log.info("Deleting user from index: {}", userId);
        userSearchRepository.deleteById(userId);
    }

    public void updateJob(JobSearchDocument job) {
        log.info("Updating job in index: {}", job.getId());
        job.setUpdatedAt(LocalDateTime.now());
        jobSearchRepository.save(job);
    }

    public void updateUser(UserSearchDocument user) {
        log.info("Updating user in index: {}", user.getId());
        user.setUpdatedAt(LocalDateTime.now());
        userSearchRepository.save(user);
    }

    // Analytics Methods
    public List<String> getPopularSkills(int limit) {
        // This would typically use aggregations
        // For now, return a simple list
        return List.of("React", "Node.js", "Python", "Java", "JavaScript", "TypeScript", "AWS", "Docker");
    }

    public List<String> getPopularLocations(int limit) {
        // This would typically use aggregations
        return List.of("New York", "San Francisco", "London", "Berlin", "Tokyo", "Remote");
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Sort.Direction direction = Sort.Direction.ASC;
            if ("desc".equalsIgnoreCase(sortDirection)) {
                direction = Sort.Direction.DESC;
            }
            sort = Sort.by(direction, sortBy);
        }
        return PageRequest.of(page, size, sort);
    }
}
