package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 最佳实践团队DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BestPracticeTeam {
    
    /**
     * 团队ID
     */
    private Long teamId;
    
    /**
     * 团队名称
     */
    private String teamName;
    
    /**
     * 最佳实践类别
     */
    private String category;
    
    /**
     * 实践描述
     */
    private String practiceDescription;
    
    /**
     * 关键指标
     */
    private String keyMetric;
    
    /**
     * 指标值
     */
    private Double metricValue;
    
    /**
     * 推荐理由
     */
    private String recommendationReason;
}