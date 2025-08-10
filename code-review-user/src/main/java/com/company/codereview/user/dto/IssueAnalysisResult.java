package com.company.codereview.user.dto;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 问题分析结果DTO
 */
@Data
@Builder
public class IssueAnalysisResult {
    
    /**
     * 问题类型分布统计
     */
    private Map<IssueType, Long> typeDistribution;
    
    /**
     * 严重级别分布统计
     */
    private Map<Severity, Long> severityDistribution;
    
    /**
     * 高频问题列表
     */
    private List<FrequentIssue> frequentIssues;
    
    /**
     * 趋势分析结果
     */
    private TrendAnalysis trendAnalysis;
    
    /**
     * 问题模式识别结果
     */
    private List<IssuePattern> patterns;
    
    /**
     * 问题聚类结果
     */
    private List<IssueCluster> clusters;
    
    /**
     * 分析时间范围
     */
    private LocalDate startDate;
    private LocalDate endDate;
    
    /**
     * 总问题数量
     */
    private Long totalIssues;
    
    /**
     * 已解决问题数量
     */
    private Long resolvedIssues;
    
    /**
     * 解决率
     */
    private Double resolutionRate;
}