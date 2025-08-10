package com.company.codereview.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.company.codereview.common.entity.BaseEntity;
import com.company.codereview.user.dto.AISummaryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * AI Summary Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_summaries")
public class AISummary extends BaseEntity {
    
    /**
     * Primary key ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * Summary title
     */
    @TableField("title")
    private String title;
    
    /**
     * Summary content
     */
    @TableField("content")
    private String content;
    
    /**
     * Summary type
     */
    @TableField("summary_type")
    private AISummaryRequest.SummaryType summaryType;
    
    /**
     * Team ID (null for global summary)
     */
    @TableField("team_id")
    private Long teamId;
    
    /**
     * Analysis start date
     */
    @TableField("start_date")
    private LocalDate startDate;
    
    /**
     * Analysis end date
     */
    @TableField("end_date")
    private LocalDate endDate;
    
    /**
     * Key insights (JSON format)
     */
    @TableField("key_insights")
    private String keyInsights;
    
    /**
     * Recommendations (JSON format)
     */
    @TableField("recommendations")
    private String recommendations;
    
    /**
     * Summary statistics (JSON format)
     */
    @TableField("statistics")
    private String statistics;
    
    /**
     * AI model used
     */
    @TableField("ai_model")
    private String aiModel;
    
    /**
     * Confidence score
     */
    @TableField("confidence")
    private Double confidence;
    
    /**
     * Processing time in milliseconds
     */
    @TableField("processing_time_ms")
    private Long processingTimeMs;
    
    /**
     * Summary status
     */
    @TableField("status")
    private SummaryStatus status;
    
    /**
     * Published flag
     */
    @TableField("published")
    private Boolean published;
    
    /**
     * Published at
     */
    @TableField("published_at")
    private java.time.LocalDateTime publishedAt;
    
    /**
     * Published by
     */
    @TableField("published_by")
    private Long publishedBy;
    
    /**
     * Version
     */
    @TableField("version")
    private String version;
    
    /**
     * Summary Status Enum
     */
    public enum SummaryStatus {
        GENERATING("Generating"),
        COMPLETED("Completed"),
        FAILED("Failed"),
        DRAFT("Draft"),
        PUBLISHED("Published"),
        ARCHIVED("Archived");
        
        private final String description;
        
        SummaryStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}