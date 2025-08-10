package com.company.codereview.user.dto;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import lombok.Builder;
import lombok.Data;

/**
 * 高频问题DTO
 */
@Data
@Builder
public class FrequentIssue {
    
    /**
     * 问题描述关键词
     */
    private String keyword;
    
    /**
     * 出现次数
     */
    private Long count;
    
    /**
     * 问题类型
     */
    private IssueType issueType;
    
    /**
     * 平均严重级别
     */
    private Severity averageSeverity;
    
    /**
     * 相似问题示例
     */
    private String example;
    
    /**
     * 建议解决方案
     */
    private String suggestedSolution;
    
    /**
     * 影响范围（百分比）
     */
    private Double impactPercentage;
}