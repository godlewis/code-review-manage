package com.company.codereview.user.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * AI Summary Request DTO
 */
@Data
public class AISummaryRequest {
    
    /**
     * Team ID (optional, null for global summary)
     */
    private Long teamId;
    
    /**
     * Start date
     */
    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;
    
    /**
     * End date
     */
    @NotNull(message = "End date cannot be null")
    private LocalDate endDate;
    
    /**
     * Summary type
     */
    @NotNull(message = "Summary type cannot be null")
    private SummaryType summaryType;
    
    /**
     * Include analysis details
     */
    private Boolean includeAnalysis = true;
    
    /**
     * Include recommendations
     */
    private Boolean includeRecommendations = true;
    
    /**
     * Custom prompt (optional)
     */
    private String customPrompt;
    
    /**
     * Summary Type Enum
     */
    public enum SummaryType {
        TEAM_WEEKLY("Team Weekly Summary"),
        TEAM_MONTHLY("Team Monthly Summary"),
        ARCHITECT_WEEKLY("Architect Weekly Summary"),
        ARCHITECT_MONTHLY("Architect Monthly Summary"),
        CUSTOM("Custom Summary");
        
        private final String description;
        
        SummaryType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}