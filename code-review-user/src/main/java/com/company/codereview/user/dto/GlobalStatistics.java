package com.company.codereview.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 全局统计数据DTO
 */
@Data
@Builder
public class GlobalStatistics {
    
    /**
     * 总团队数量
     */
    private Integer totalTeams;
    
    /**
     * 总用户数量
     */
    private Integer totalUsers;
    
    /**
     * 活跃用户数量
     */
    private Integer activeUsers;
    
    /**
     * 总评审数量
     */
    private Long totalReviews;
    
    /**
     * 总问题数量
     */
    private Long totalIssues;
    
    /**
     * 全局问题解决率
     */
    private Double globalResolutionRate;
    
    /**
     * 全局平均评审分数
     */
    private Double globalAverageScore;
    
    /**
     * 团队表现排名
     */
    private List<TeamPerformance> teamRankings;
    
    /**
     * 跨团队问题分布
     */
    private Map<String, Long> crossTeamIssueDistribution;
    
    /**
     * 系统使用趋势
     */
    private List<UsageTrend> usageTrends;
    
    /**
     * 质量改进趋势
     */
    private List<QualityImprovementTrend> qualityTrends;
    
    /**
     * 最佳实践团队
     */
    private List<BestPracticeTeam> bestPracticeTeams;
}