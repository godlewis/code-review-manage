package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

/**
 * 月度对比数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyComparison {
    
    /**
     * 年月
     */
    private YearMonth yearMonth;
    
    /**
     * 当月数据
     */
    private MonthlyStatistics currentMonth;
    
    /**
     * 上月数据
     */
    private MonthlyStatistics previousMonth;
    
    /**
     * 评审数量变化率
     */
    private Double reviewCountChangeRate;
    
    /**
     * 问题数量变化率
     */
    private Double issueCountChangeRate;
    
    /**
     * 质量分数变化率
     */
    private Double scoreChangeRate;
    
    /**
     * 完成率变化率
     */
    private Double completionRateChange;
}