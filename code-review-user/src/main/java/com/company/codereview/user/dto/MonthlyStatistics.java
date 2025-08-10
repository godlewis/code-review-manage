package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

/**
 * 月度统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyStatistics {
    
    /**
     * 年月
     */
    private YearMonth yearMonth;
    
    /**
     * 评审数量
     */
    private Long reviewCount;
    
    /**
     * 问题数量
     */
    private Long issueCount;
    
    /**
     * 平均评审分数
     */
    private Double averageScore;
    
    /**
     * 完成率
     */
    private Double completionRate;
    
    /**
     * 整改及时率
     */
    private Double fixTimeliness;
}