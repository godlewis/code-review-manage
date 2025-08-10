package com.company.codereview.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 问题分析配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "issue.analysis")
public class IssueAnalysisConfig {
    
    /**
     * 高频问题识别配置
     */
    private FrequentIssueConfig frequentIssue = new FrequentIssueConfig();
    
    /**
     * 聚类分析配置
     */
    private ClusterConfig cluster = new ClusterConfig();
    
    /**
     * 趋势分析配置
     */
    private TrendConfig trend = new TrendConfig();
    
    /**
     * 模式识别配置
     */
    private PatternConfig pattern = new PatternConfig();
    
    /**
     * 缓存配置
     */
    private CacheConfig cache = new CacheConfig();
    
    @Data
    public static class FrequentIssueConfig {
        /**
         * 高频问题最小出现次数
         */
        private int minOccurrence = 2;
        
        /**
         * 返回的高频问题数量限制
         */
        private int maxResults = 10;
        
        /**
         * 关键词最小长度
         */
        private int minKeywordLength = 2;
        
        /**
         * 停用词列表
         */
        private String[] stopWords = {
            "的", "是", "在", "有", "和", "或", "但", "如果", "因为", "所以",
            "the", "is", "in", "and", "or", "but", "if", "because", "so"
        };
    }
    
    @Data
    public static class ClusterConfig {
        /**
         * 默认聚类数量
         */
        private int defaultClusterCount = 5;
        
        /**
         * 最小聚类大小
         */
        private int minClusterSize = 2;
        
        /**
         * K-means最大迭代次数
         */
        private int maxIterations = 100;
        
        /**
         * 相似度阈值
         */
        private double similarityThreshold = 0.3;
        
        /**
         * 聚类权重计算参数
         */
        private WeightConfig weight = new WeightConfig();
        
        @Data
        public static class WeightConfig {
            private double quantityWeight = 0.6;
            private double severityWeight = 0.4;
        }
    }
    
    @Data
    public static class TrendConfig {
        /**
         * 趋势分析最小数据点数量
         */
        private int minDataPoints = 2;
        
        /**
         * 趋势方向判断阈值
         */
        private double trendThreshold = 0.1;
        
        /**
         * 质量评分计算参数
         */
        private QualityScoreConfig qualityScore = new QualityScoreConfig();
        
        @Data
        public static class QualityScoreConfig {
            private double criticalDeduction = 10.0;
            private double majorDeduction = 5.0;
            private double minorDeduction = 2.0;
            private double suggestionDeduction = 0.5;
            private double baseScore = 100.0;
        }
    }
    
    @Data
    public static class PatternConfig {
        /**
         * 模式识别最小频率
         */
        private long minFrequency = 2;
        
        /**
         * 模式置信度阈值
         */
        private double confidenceThreshold = 0.7;
        
        /**
         * 关键词匹配配置
         */
        private KeywordMatchConfig keywordMatch = new KeywordMatchConfig();
        
        @Data
        public static class KeywordMatchConfig {
            /**
             * 空指针模式关键词
             */
            private String[] nullPointerKeywords = {"null", "空指针", "nullpointer", "npe"};
            
            /**
             * 性能问题模式关键词
             */
            private String[] performanceKeywords = {"性能", "慢", "超时", "内存", "cpu", "performance", "slow"};
            
            /**
             * 安全漏洞模式关键词
             */
            private String[] securityKeywords = {"安全", "注入", "xss", "csrf", "权限", "认证", "加密"};
            
            /**
             * 代码规范模式关键词
             */
            private String[] standardKeywords = {"命名", "格式", "注释", "规范", "风格", "style"};
        }
    }
    
    @Data
    public static class CacheConfig {
        /**
         * 缓存过期时间（分钟）
         */
        private int expireMinutes = 30;
        
        /**
         * 最大缓存大小
         */
        private int maxSize = 1000;
        
        /**
         * 是否启用缓存
         */
        private boolean enabled = true;
    }
    
    /**
     * 优先级计算配置
     */
    @Data
    public static class PriorityConfig {
        /**
         * 严重级别权重
         */
        private double severityWeight = 0.5;
        
        /**
         * 问题类型权重
         */
        private double typeWeight = 0.3;
        
        /**
         * 问题年龄权重
         */
        private double ageWeight = 0.2;
        
        /**
         * 严重级别评分
         */
        private SeverityScore severityScore = new SeverityScore();
        
        /**
         * 问题类型评分
         */
        private TypeScore typeScore = new TypeScore();
        
        @Data
        public static class SeverityScore {
            private double critical = 100.0;
            private double major = 70.0;
            private double minor = 40.0;
            private double suggestion = 10.0;
        }
        
        @Data
        public static class TypeScore {
            private double securityVulnerability = 100.0;
            private double functionalDefect = 80.0;
            private double performanceIssue = 60.0;
            private double designIssue = 40.0;
            private double codeStandard = 20.0;
        }
    }
    
    /**
     * 效率计算配置
     */
    @Data
    public static class EfficiencyConfig {
        /**
         * 逾期天数阈值
         */
        private int overdueDaysThreshold = 7;
        
        /**
         * 问题年龄评分阈值
         */
        private AgeThreshold ageThreshold = new AgeThreshold();
        
        @Data
        public static class AgeThreshold {
            private int level1Days = 1;
            private int level2Days = 3;
            private int level3Days = 7;
            private int level4Days = 14;
            
            private double level1Score = 10.0;
            private double level2Score = 30.0;
            private double level3Score = 50.0;
            private double level4Score = 70.0;
            private double level5Score = 100.0;
        }
    }
}