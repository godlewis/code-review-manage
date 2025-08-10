package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 质量趋势点DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityPoint {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 平均质量分数
     */
    private Double averageQualityScore;
    
    /**
     * 问题密度（问题数/评审数）
     */
    private Double issueDensity;
    
    /**
     * 严重问题比例
     */
    private Double criticalIssueRatio;
    
    /**
     * 问题解决率
     */
    private Double resolutionRate;
}