package com.microjobs.jobs.web;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateJobRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;
    
    @NotNull(message = "Budget amount is required")
    @DecimalMin(value = "0.01", message = "Budget amount must be greater than 0")
    private BigDecimal budgetAmount;
    
    @NotBlank(message = "Budget currency is required")
    private String budgetCurrency;
    
    private LocalDateTime deadline;
    
    private List<String> requiredSkills;
    
    private String location;
    private Double latitude;
    private Double longitude;
    private Integer maxDistanceKm;
}
