package com.microjobs.search.ports;

import com.microjobs.search.domain.UserSearchDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSearchRepository extends ElasticsearchRepository<UserSearchDocument, String> {

    // Search by name
    Page<UserSearchDocument> findByFirstNameContainingOrLastNameContaining(
            String firstName, String lastName, Pageable pageable);

    // Search by skills
    Page<UserSearchDocument> findBySkillsIn(List<String> skills, Pageable pageable);

    // Search by specializations
    Page<UserSearchDocument> findBySpecializationsIn(List<String> specializations, Pageable pageable);

    // Search by user type
    Page<UserSearchDocument> findByUserType(String userType, Pageable pageable);

    // Search by status
    Page<UserSearchDocument> findByStatus(String status, Pageable pageable);

    // Search by location
    @Query("{\"bool\": {\"must\": [{\"match\": {\"location\": \"?0\"}}], \"filter\": [{\"geo_distance\": {\"distance\": \"?1km\", \"geoLocation\": {\"lat\": ?2, \"lon\": ?3}}}]}}")
    Page<UserSearchDocument> findByLocationNear(String location, Double distance,
                                              Double latitude, Double longitude, Pageable pageable);

    // Search by hourly rate range
    @Query("{\"bool\": {\"filter\": [{\"range\": {\"hourlyRate\": {\"gte\": ?0, \"lte\": ?1}}}]}}")
    Page<UserSearchDocument> findByHourlyRateBetween(Double minRate, Double maxRate, Pageable pageable);

    // Search by experience
    Page<UserSearchDocument> findByExperienceYearsGreaterThanEqual(Integer minExperience, Pageable pageable);

    // Search by rating
    Page<UserSearchDocument> findByRatingGreaterThanEqual(Double minRating, Pageable pageable);

    // Search available users
    Page<UserSearchDocument> findByIsAvailableTrue(Pageable pageable);

    // Search verified users
    Page<UserSearchDocument> findByIsVerifiedTrue(Pageable pageable);

    // Complex search with multiple criteria
    @Query("{\"bool\": {\"must\": [{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"firstName\", \"lastName\", \"bio\", \"skills\"]}}, {\"terms\": {\"skills\": ?1}}, {\"term\": {\"userType\": \"?2\"}}, {\"term\": {\"isAvailable\": true}}]}}")
    Page<UserSearchDocument> searchUsersWithFilters(String query, List<String> skills, String userType, Pageable pageable);

    // Search by tenant
    Page<UserSearchDocument> findByTenantId(String tenantId, Pageable pageable);
}
