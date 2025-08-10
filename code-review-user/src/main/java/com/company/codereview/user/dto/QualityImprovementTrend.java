package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 质量改进趋势DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityImprovementTrend {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 全局平均质量分数
     */
    private Double globalAverageScore;
    
    /**
     * 问题解决率
     */
    private Double resolutionRate;
    
    /**
     * 严重问题比例
     */
    private Double criticalIssueRatio;
    
    /**
     * 质量改进指数
     */
    private Double qualityImprovementIndex;
}