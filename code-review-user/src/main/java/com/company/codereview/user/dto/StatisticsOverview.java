package com.company.codereview.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 统计概览DTO
 */
@Data
@Builder
public class StatisticsOverview {
    
    /**
     * 个人统计数据
     */
    private PersonalStatistics personalStats;
    
    /**
     * 团队统计数据（如果用户是团队负责人）
     */
    private TeamStatistics teamStats;
    
    /**
     * 全局统计数据（如果用户是架构师）
     */
    private GlobalStatistics globalStats;
    
    /**
     * 快速指标
     */
    private QuickMetrics quickMetrics;
    
    /**
     * 快速指标内部类
     */
    @Data
    @Builder
    public static class QuickMetrics {
        
        /**
         * 今日评审数量
         */
        private Long todayReviews;
        
        /**
         * 待处理问题数量
         */
        private Long pendingIssues;
        
        /**
         * 本周完成率
         */
        private Double weeklyCompletionRate;
        
        /**
         * 质量趋势（上升/下降/稳定）
         */
        private String qualityTrend;
        
        /**
         * 排名变化
         */
        private Integer rankingChange;
    }
}