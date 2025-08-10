package com.company.codereview.user.service;

import com.company.codereview.user.dto.IssueAnalysisResult;
import com.company.codereview.user.entity.Issue;
import com.company.codereview.user.repository.IssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题分析服务
 * 整合问题数据查询和分析功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueAnalysisService {
    
    private final IssueRepository issueRepository;
    private final IssueAnalyzer issueAnalyzer;
    
    /**
     * 分析团队问题
     * 
     * @param teamId 团队ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 问题分析结果
     */
    @Cacheable(value = "issue-analysis", key = "#teamId + '-' + #startDate + '-' + #endDate")
    public IssueAnalysisResult analyzeTeamIssues(Long teamId, LocalDate startDate, LocalDate endDate) {
        log.info("开始分析团队问题，团队ID: {}, 时间范围: {} - {}", teamId, startDate, endDate);
        
        // 查询团队在指定时间范围内的所有问题
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        List<Issue> issues = issueRepository.findByTeamId(teamId, startDateTime, endDateTime);
        
        if (issues.isEmpty()) {
            log.info("团队 {} 在指定时间范围内没有问题记录", teamId);
            return createEmptyAnalysisResult(startDate, endDate);
        }
        
        // 使用问题分析器进行分析
        IssueAnalysisResult result = issueAnalyzer.analyzeTeamIssues(issues, startDate, endDate);
        
        log.info("团队问题分析完成，共分析 {} 个问题，识别出 {} 个模式，{} 个聚类", 
            result.getTotalIssues(), 
            result.getPatterns().size(), 
            result.getClusters().size());
        
        return result;
    }
    
    /**
     * 分析用户个人问题
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 问题分析结果
     */
    @Cacheable(value = "user-issue-analysis", key = "#userId + '-' + #startDate + '-' + #endDate")
    public IssueAnalysisResult analyzeUserIssues(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("开始分析用户问题，用户ID: {}, 时间范围: {} - {}", userId, startDate, endDate);
        
        // 查询用户在指定时间范围内分配的所有问题
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 这里需要根据实际的数据库查询方法来获取用户相关的问题
        // 假设我们有一个方法可以查询分配给用户的问题
        List<Issue> issues = issueRepository.findAssignedToUser(userId, null); // 查询所有状态的问题
        
        // 过滤时间范围
        issues = issues.stream()
            .filter(issue -> {
                LocalDateTime createdAt = issue.getCreatedAt();
                return createdAt != null && 
                       !createdAt.isBefore(startDateTime) && 
                       !createdAt.isAfter(endDateTime);
            })
            .collect(java.util.stream.Collectors.toList());
        
        if (issues.isEmpty()) {
            log.info("用户 {} 在指定时间范围内没有问题记录", userId);
            return createEmptyAnalysisResult(startDate, endDate);
        }
        
        // 使用问题分析器进行分析
        IssueAnalysisResult result = issueAnalyzer.analyzeTeamIssues(issues, startDate, endDate);
        
        log.info("用户问题分析完成，共分析 {} 个问题", result.getTotalIssues());
        
        return result;
    }
    
    /**
     * 分析全局问题（架构师视图）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 问题分析结果
     */
    @Cacheable(value = "global-issue-analysis", key = "#startDate + '-' + #endDate")
    public IssueAnalysisResult analyzeGlobalIssues(LocalDate startDate, LocalDate endDate) {
        log.info("开始分析全局问题，时间范围: {} - {}", startDate, endDate);
        
        // 查询所有团队在指定时间范围内的问题
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 这里需要一个查询所有问题的方法，暂时使用团队ID为null来表示查询所有
        List<Issue> issues = issueRepository.findByTeamId(null, startDateTime, endDateTime);
        
        if (issues.isEmpty()) {
            log.info("指定时间范围内没有问题记录");
            return createEmptyAnalysisResult(startDate, endDate);
        }
        
        // 使用问题分析器进行分析
        IssueAnalysisResult result = issueAnalyzer.analyzeTeamIssues(issues, startDate, endDate);
        
        log.info("全局问题分析完成，共分析 {} 个问题，涉及 {} 个模式", 
            result.getTotalIssues(), 
            result.getPatterns().size());
        
        return result;
    }
    
    /**
     * 比较两个时间段的问题分析结果
     * 
     * @param teamId 团队ID
     * @param currentStartDate 当前时间段开始日期
     * @param currentEndDate 当前时间段结束日期
     * @param previousStartDate 对比时间段开始日期
     * @param previousEndDate 对比时间段结束日期
     * @return 比较结果
     */
    public IssueAnalysisComparison compareAnalysisResults(Long teamId, 
                                                         LocalDate currentStartDate, LocalDate currentEndDate,
                                                         LocalDate previousStartDate, LocalDate previousEndDate) {
        log.info("开始比较问题分析结果，团队ID: {}", teamId);
        
        IssueAnalysisResult currentResult = analyzeTeamIssues(teamId, currentStartDate, currentEndDate);
        IssueAnalysisResult previousResult = analyzeTeamIssues(teamId, previousStartDate, previousEndDate);
        
        return IssueAnalysisComparison.builder()
            .currentPeriod(currentResult)
            .previousPeriod(previousResult)
            .issueCountChange(currentResult.getTotalIssues() - previousResult.getTotalIssues())
            .resolutionRateChange(currentResult.getResolutionRate() - previousResult.getResolutionRate())
            .newPatterns(findNewPatterns(currentResult, previousResult))
            .improvedAreas(findImprovedAreas(currentResult, previousResult))
            .worsenedAreas(findWorsenedAreas(currentResult, previousResult))
            .build();
    }
    
    /**
     * 创建空的分析结果
     */
    private IssueAnalysisResult createEmptyAnalysisResult(LocalDate startDate, LocalDate endDate) {
        return IssueAnalysisResult.builder()
            .typeDistribution(new java.util.HashMap<>())
            .severityDistribution(new java.util.HashMap<>())
            .frequentIssues(new java.util.ArrayList<>())
            .trendAnalysis(createEmptyTrendAnalysis(startDate, endDate))
            .patterns(new java.util.ArrayList<>())
            .clusters(new java.util.ArrayList<>())
            .startDate(startDate)
            .endDate(endDate)
            .totalIssues(0L)
            .resolvedIssues(0L)
            .resolutionRate(0.0)
            .build();
    }
    
    /**
     * 创建空的趋势分析
     */
    private com.company.codereview.user.dto.TrendAnalysis createEmptyTrendAnalysis(LocalDate startDate, LocalDate endDate) {
        return com.company.codereview.user.dto.TrendAnalysis.builder()
            .issueTrend(new java.util.ArrayList<>())
            .resolutionTrend(new java.util.ArrayList<>())
            .qualityTrend(new java.util.ArrayList<>())
            .overallDirection(com.company.codereview.user.dto.TrendAnalysis.TrendDirection.STABLE)
            .changeRate(0.0)
            .predictedIssues(0L)
            .summary("暂无数据进行趋势分析")
            .build();
    }
    
    /**
     * 查找新出现的模式
     */
    private java.util.List<String> findNewPatterns(IssueAnalysisResult current, IssueAnalysisResult previous) {
        java.util.Set<String> currentPatterns = current.getPatterns().stream()
            .map(com.company.codereview.user.dto.IssuePattern::getPatternId)
            .collect(java.util.stream.Collectors.toSet());
        
        java.util.Set<String> previousPatterns = previous.getPatterns().stream()
            .map(com.company.codereview.user.dto.IssuePattern::getPatternId)
            .collect(java.util.stream.Collectors.toSet());
        
        return currentPatterns.stream()
            .filter(pattern -> !previousPatterns.contains(pattern))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 查找改善的领域
     */
    private java.util.List<String> findImprovedAreas(IssueAnalysisResult current, IssueAnalysisResult previous) {
        java.util.List<String> improvedAreas = new java.util.ArrayList<>();
        
        // 比较各类型问题的数量变化
        for (com.company.codereview.common.enums.IssueType type : com.company.codereview.common.enums.IssueType.values()) {
            Long currentCount = current.getTypeDistribution().getOrDefault(type, 0L);
            Long previousCount = previous.getTypeDistribution().getOrDefault(type, 0L);
            
            if (currentCount < previousCount) {
                improvedAreas.add(type.getDescription() + "问题减少");
            }
        }
        
        return improvedAreas;
    }
    
    /**
     * 查找恶化的领域
     */
    private java.util.List<String> findWorsenedAreas(IssueAnalysisResult current, IssueAnalysisResult previous) {
        java.util.List<String> worsenedAreas = new java.util.ArrayList<>();
        
        // 比较各类型问题的数量变化
        for (com.company.codereview.common.enums.IssueType type : com.company.codereview.common.enums.IssueType.values()) {
            Long currentCount = current.getTypeDistribution().getOrDefault(type, 0L);
            Long previousCount = previous.getTypeDistribution().getOrDefault(type, 0L);
            
            if (currentCount > previousCount) {
                worsenedAreas.add(type.getDescription() + "问题增加");
            }
        }
        
        return worsenedAreas;
    }
    
    /**
     * 问题分析比较结果DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class IssueAnalysisComparison {
        private IssueAnalysisResult currentPeriod;
        private IssueAnalysisResult previousPeriod;
        private Long issueCountChange;
        private Double resolutionRateChange;
        private java.util.List<String> newPatterns;
        private java.util.List<String> improvedAreas;
        private java.util.List<String> worsenedAreas;
    }
}