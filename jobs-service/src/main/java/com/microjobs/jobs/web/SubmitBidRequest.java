package com.microjobs.jobs.web;

import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

@Data
public class SubmitBidRequest {
    
    @NotBlank(message = "Worker ID is required")
    private String workerId;
    
    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    private BigDecimal bidAmount;
    
    @NotBlank(message = "Bid currency is required")
    private String bidCurrency;
    
    @NotBlank(message = "Proposal is required")
    @Size(min = 10, max = 500, message = "Proposal must be between 10 and 500 characters")
    private String proposal;
    
    @NotNull(message = "Estimated completion days is required")
    @Min(value = 1, message = "Estimated completion days must be at least 1")
    @Max(value = 365, message = "Estimated completion days cannot exceed 365")
    private Integer estimatedCompletionDays;
}
