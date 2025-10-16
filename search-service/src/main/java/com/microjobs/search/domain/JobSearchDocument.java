package com.microjobs.search.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Document(indexName = "microjobs-jobs")
@Data
@NoArgsConstructor
public class JobSearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String tenantId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Double)
    private BigDecimal budgetAmount;

    @Field(type = FieldType.Keyword)
    private String budgetCurrency;

    @Field(type = FieldType.Date)
    private LocalDateTime deadline;

    @Field(type = FieldType.Keyword)
    private List<String> requiredSkills;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String location;

    @GeoPointField
    private String geoLocation;

    @Field(type = FieldType.Double)
    private Double maxDistanceKm;

    @Field(type = FieldType.Keyword)
    private String clientId;

    @Field(type = FieldType.Keyword)
    private String assignedWorkerId;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Date)
    private LocalDateTime assignedAt;

    @Field(type = FieldType.Date)
    private LocalDateTime completedAt;

    @Field(type = FieldType.Integer)
    private Integer bidCount;

    @Field(type = FieldType.Double)
    private Double averageBidAmount;

    @Field(type = FieldType.Text, analyzer = "keyword")
    private String category;

    @Field(type = FieldType.Text, analyzer = "keyword")
    private String subcategory;

    @Field(type = FieldType.Boolean)
    private Boolean isUrgent;

    @Field(type = FieldType.Boolean)
    private Boolean isRemote;

    @Field(type = FieldType.Integer)
    private Integer experienceLevel;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String tags;

    public JobSearchDocument(String id, String tenantId, String title, String description,
                           BigDecimal budgetAmount, String budgetCurrency, LocalDateTime deadline,
                           List<String> requiredSkills, String location, String geoLocation,
                           Double maxDistanceKm, String clientId, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.title = title;
        this.description = description;
        this.budgetAmount = budgetAmount;
        this.budgetCurrency = budgetCurrency;
        this.deadline = deadline;
        this.requiredSkills = requiredSkills;
        this.location = location;
        this.geoLocation = geoLocation;
        this.maxDistanceKm = maxDistanceKm;
        this.clientId = clientId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.bidCount = 0;
        this.isUrgent = false;
        this.isRemote = false;
        this.experienceLevel = 1;
    }
}
