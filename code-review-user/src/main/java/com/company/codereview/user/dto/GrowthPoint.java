package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 成长趋势点DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthPoint {
    
    /**
     * 日期
     */
    private LocalDate date;
    
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
}