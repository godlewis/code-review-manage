package com.company.codereview.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 团队统计数据DTO
 */
@Data
@Builder
public class TeamStatistics {
    
    /**
     * 团队评审覆盖率 (0-1)
     */
    private Double coverageRate;
    
    /**
     * 团队平均评审分数
     */
    private Double averageScore;
    
    /**
     * 总问题数量
     */
    private Long totalIssues;
    
    /**
     * 已解决问题数量
     */
    private Long resolvedIssues;
    
    /**
     * 问题解决率 (0-1)
     */
    private Double resolutionRate;
    
    /**
     * 团队成员数量
     */
    private Integer memberCount;
    
    /**
     * 活跃成员数量
     */
    private Integer activeMemberCount;
    
    /**
     * 问题分布统计
     */
    private Map<String, Long> issueDistribution;
    
    /**
     * 严重级别分布
     */
    private Map<String, Long> severityDistribution;
    
    /**
     * 代码质量趋势
     */
    private List<QualityPoint> qualityTrend;
    
    /**
     * 成员表现排名
     */
    private List<MemberPerformance> memberRankings;
    
    /**
     * 高频问题列表
     */
    private List<FrequentIssueStats> frequentIssues;
    
    /**
     * 月度对比数据
     */
    private List<MonthlyComparison> monthlyComparisons;
}