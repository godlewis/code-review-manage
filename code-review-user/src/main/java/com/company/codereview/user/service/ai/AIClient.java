package com.company.codereview.user.service.ai;

import java.util.concurrent.CompletableFuture;

/**
 * AI服务客户端接口
 * 定义AI服务的通用接口，支持多种AI服务提供商
 */
public interface AIClient {
    
    /**
     * 生成智能汇总
     * @param prompt 提示词
     * @return AI生成的汇总内容
     */
    CompletableFuture<String> generateSummary(String prompt);
    
    /**
     * 分析问题模式
     * @param issuesData 问题数据
     * @return 问题分析结果
     */
    CompletableFuture<String> analyzeIssuePatterns(String issuesData);
    
    /**
     * 生成改进建议
     * @param analysisData 分析数据
     * @return 改进建议
     */
    CompletableFuture<String> generateImprovementSuggestions(String analysisData);
    
    /**
     * 检查服务是否可用
     * @return 服务可用性状态
     */
    boolean isAvailable();
}