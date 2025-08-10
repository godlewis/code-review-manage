package com.company.codereview.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 系统使用趋势DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageTrend {
    
    /**
     * 日期
     */
    private LocalDate date;
    
    /**
     * 活跃用户数
     */
    private Integer activeUsers;
    
    /**
     * 评审数量
     */
    private Long reviewCount;
    
    /**
     * 问题数量
     */
    private Long issueCount;
    
    /**
     * 系统使用率
     */
    private Double usageRate;
}