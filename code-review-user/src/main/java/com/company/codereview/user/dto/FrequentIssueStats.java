package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 高频问题统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequentIssueStats {
    
    /**
     * 问题描述
     */
    private String issueDescription;
    
    /**
     * 问题类型
     */
    private String issueType;
    
    /**
     * 出现次数
     */
    private Long occurrenceCount;
    
    /**
     * 占比
     */
    private Double percentage;
    
    /**
     * 平均严重级别
     */
    private String averageSeverity;
    
    /**
     * 解决率
     */
    private Double resolutionRate;
}