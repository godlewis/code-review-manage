package com.company.codereview.user.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.service")
public class AIServiceConfig {
    
    /**
     * AI服务提供商类型
     */
    private String provider = "openai";
    
    /**
     * API密钥
     */
    private String apiKey;
    
    /**
     * API基础URL
     */
    private String baseUrl = "https://api.openai.com/v1";
    
    /**
     * 默认模型
     */
    private String defaultModel = "gpt-3.5-turbo";
    
    /**
     * 请求超时时间（秒）
     */
    private int timeout = 30;
    
    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
    
    /**
     * 是否启用AI服务
     */
    private boolean enabled = true;
    
    /**
     * 熔断器配置
     */
    private CircuitBreakerConfig circuitBreaker = new CircuitBreakerConfig();
    
    /**
     * 限流配置
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();
    
    @Data
    public static class CircuitBreakerConfig {
        /**
         * 失败率阈值（百分比）
         */
        private double failureRateThreshold = 50.0;
        
        /**
         * 最小请求数
         */
        private int minimumNumberOfCalls = 10;
        
        /**
         * 等待时间（秒）
         */
        private int waitDurationInOpenState = 60;
        
        /**
         * 半开状态下的允许请求数
         */
        private int permittedNumberOfCallsInHalfOpenState = 3;
    }
    
    @Data
    public static class RateLimitConfig {
        /**
         * 每分钟最大请求数
         */
        private int requestsPerMinute = 60;
        
        /**
         * 每小时最大请求数
         */
        private int requestsPerHour = 1000;
        
        /**
         * 每天最大请求数
         */
        private int requestsPerDay = 10000;
    }
}