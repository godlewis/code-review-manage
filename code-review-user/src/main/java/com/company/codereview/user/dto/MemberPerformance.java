package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 成员表现DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPerformance {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 评审完成率
     */
    private Double completionRate;
    
    /**
     * 问题发现数量
     */
    private Long issuesFound;
    
    /**
     * 平均评审分数
     */
    private Double averageScore;
    
    /**
     * 整改及时率
     */
    private Double fixTimeliness;
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 综合评分
     */
    private Double overallScore;
}