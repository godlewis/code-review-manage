package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分配配对DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentPair {
    
    /**
     * 评审者ID
     */
    private Long reviewerId;
    
    /**
     * 被评审者ID
     */
    private Long revieweeId;
    
    /**
     * 匹配度分数
     */
    private double matchScore;
    
    /**
     * 技能匹配度
     */
    private double skillMatchScore;
    
    /**
     * 负载均衡分数
     */
    private double loadBalanceScore;
    
    /**
     * 多样性分数
     */
    private double diversityScore;
}