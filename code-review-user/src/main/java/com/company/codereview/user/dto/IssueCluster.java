package com.company.codereview.user.dto;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 问题聚类DTO
 */
@Data
@Builder
public class IssueCluster {
    
    /**
     * 聚类ID
     */
    private String clusterId;
    
    /**
     * 聚类名称
     */
    private String clusterName;
    
    /**
     * 聚类中心描述
     */
    private String centerDescription;
    
    /**
     * 聚类中的问题数量
     */
    private Long issueCount;
    
    /**
     * 主要问题类型
     */
    private IssueType dominantType;
    
    /**
     * 主要严重级别
     */
    private Severity dominantSeverity;
    
    /**
     * 聚类内问题ID列表
     */
    private List<Long> issueIds;
    
    /**
     * 聚类特征关键词
     */
    private List<String> characteristicKeywords;
    
    /**
     * 聚类内相似度（0-1）
     */
    private Double similarity;
    
    /**
     * 聚类建议
     */
    private String recommendation;
    
    /**
     * 聚类权重（表示重要性）
     */
    private Double weight;
}