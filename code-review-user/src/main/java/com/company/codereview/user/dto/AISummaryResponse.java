package com.company.codereview.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Summary Response DTO
 */
@Data
@Builder
public class AISummaryResponse {
    
    /**
     * Summary ID
     */
    private Long summaryId;
    
    /**
     * Summary title
     */
    private String title;
    
    /**
     * Summary content
     */
    private String content;
    
    /**
     * Summary type
     */
    private AISummaryRequest.SummaryType summaryType;
    
    /**
     * Team ID (null for global summary)
     */
    private Long teamId;
    
    /**
     * Team name
     */
    private String teamName;
    
    /**
     * Analysis period
     */
    private LocalDate startDate;
    private LocalDate endDate;
    
    /**
     * Issue analysis result
     */
    private IssueAnalysisResult analysisResult;
    
    /**
     * Key insights
     */
    private List<String> keyInsights;
    
    /**
     * Recommendations
     */
    private List<String> recommendations;
    
    /**
     * Summary statistics
     */
    private SummaryStatistics statistics;
    
    /**
     * Generation metadata
     */
    private GenerationMetadata metadata;
    
    /**
     * Summary Statistics
     */
    @Data
    @Builder
    public static class SummaryStatistics {
        private Long totalIssues;
        private Long resolvedIssues;
        private Double resolutionRate;
        private Long criticalIssues;
        private Long majorIssues;
        private Long minorIssues;
        private Long suggestionIssues;
        private Integer patternsIdentified;
        private Integer clustersFound;
    }
    
    /**
     * Generation Metadata
     */
    @Data
    @Builder
    public static class GenerationMetadata {
        private LocalDateTime generatedAt;
        private Long generatedBy;
        private String generatedByName;
        private String aiModel;
        private Double confidence;
        private Long processingTimeMs;
        private String version;
    }
}