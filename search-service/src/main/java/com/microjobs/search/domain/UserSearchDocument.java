package com.microjobs.search.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;

import java.time.LocalDateTime;
import java.util.List;

@Document(indexName = "microjobs-users")
@Data
@NoArgsConstructor
public class UserSearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String tenantId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String email;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String firstName;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String lastName;

    @Field(type = FieldType.Keyword)
    private String userType;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String bio;

    @Field(type = FieldType.Text, analyzer = "keyword")
    private List<String> skills;

    @Field(type = FieldType.Text, analyzer = "keyword")
    private List<String> specializations;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String location;

    @GeoPointField
    private String geoLocation;

    @Field(type = FieldType.Double)
    private Double hourlyRate;

    @Field(type = FieldType.Keyword)
    private String currency;

    @Field(type = FieldType.Integer)
    private Integer experienceYears;

    @Field(type = FieldType.Text, analyzer = "keyword")
    private String education;

    @Field(type = FieldType.Text, analyzer = "keyword")
    private List<String> certifications;

    @Field(type = FieldType.Text, analyzer = "keyword")
    private List<String> languages;

    @Field(type = FieldType.Double)
    private Double rating;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    @Field(type = FieldType.Integer)
    private Integer completedJobs;

    @Field(type = FieldType.Integer)
    private Integer activeJobs;

    @Field(type = FieldType.Boolean)
    private Boolean isAvailable;

    @Field(type = FieldType.Boolean)
    private Boolean isVerified;

    @Field(type = FieldType.Date)
    private LocalDateTime lastActiveAt;

    @Field(type = FieldType.Date)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date)
    private LocalDateTime updatedAt;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String portfolioUrl;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String linkedinUrl;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String githubUrl;

    public UserSearchDocument(String id, String tenantId, String email, String firstName, 
                           String lastName, String userType, String status) {
        this.id = id;
        this.tenantId = tenantId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.rating = 0.0;
        this.reviewCount = 0;
        this.completedJobs = 0;
        this.activeJobs = 0;
        this.isAvailable = true;
        this.isVerified = false;
    }
}
