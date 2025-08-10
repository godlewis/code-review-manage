package com.company.codereview.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 趋势分析DTO
 */
@Data
@Builder
public class TrendAnalysis {
    
    /**
     * 问题数量趋势点
     */
    private List<TrendPoint> issueTrend;
    
    /**
     * 解决率趋势点
     */
    private List<TrendPoint> resolutionTrend;
    
    /**
     * 质量评分趋势点
     */
    private List<TrendPoint> qualityTrend;
    
    /**
     * 总体趋势方向
     */
    private TrendDirection overallDirection;
    
    /**
     * 趋势变化率（百分比）
     */
    private Double changeRate;
    
    /**
     * 预测下一周期的问题数量
     */
    private Long predictedIssues;
    
    /**
     * 趋势分析总结
     */
    private String summary;
    
    /**
     * 趋势点数据
     */
    @Data
    @Builder
    public static class TrendPoint {
        private LocalDate date;
        private Double value;
        private String label;
    }
    
    /**
     * 趋势方向枚举
     */
    public enum TrendDirection {
        IMPROVING("改善中"),
        STABLE("稳定"),
        DECLINING("下降中"),
        UNKNOWN("未知");
        
        private final String description;
        
        TrendDirection(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}