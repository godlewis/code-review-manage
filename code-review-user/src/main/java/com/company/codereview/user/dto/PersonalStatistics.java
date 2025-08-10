package com.company.codereview.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 个人统计数据DTO
 */
@Data
@Builder
public class PersonalStatistics {
    
    /**
     * 评审完成率 (0-1)
     */
    private Double completionRate;
    
    /**
     * 问题发现数量
     */
    private Long issuesFound;
    
    /**
     * 整改及时率 (0-1)
     */
    private Double fixTimeliness;
    
    /**
     * 平均评审分数
     */
    private Double averageReviewScore;
    
    /**
     * 总评审次数
     */
    private Long totalReviews;
    
    /**
     * 已完成评审次数
     */
    private Long completedReviews;
    
    /**
     * 待处理问题数量
     */
    private Long pendingIssues;
    
    /**
     * 已解决问题数量
     */
    private Long resolvedIssues;
    
    /**
     * 个人成长趋势
     */
    private List<GrowthPoint> growthTrend;
    
    /**
     * 问题类型分布
     */
    private Map<String, Long> issueTypeDistribution;
    
    /**
     * 严重级别分布
     */
    private Map<String, Long> severityDistribution;
    
    /**
     * 月度统计数据
     */
    private List<MonthlyStatistics> monthlyStats;
}