package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 团队表现DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPerformance {
    
    /**
     * 团队ID
     */
    private Long teamId;
    
    /**
     * 团队名称
     */
    private String teamName;
    
    /**
     * 团队评审覆盖率
     */
    private Double coverageRate;
    
    /**
     * 平均质量分数
     */
    private Double averageScore;
    
    /**
     * 问题解决率
     */
    private Double resolutionRate;
    
    /**
     * 成员数量
     */
    private Integer memberCount;
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 综合评分
     */
    private Double overallScore;
    
    /**
     * 改进趋势
     */
    private String improvementTrend;
}