package com.company.codereview.user.service.ai;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.entity.Issue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI服务降级实现
 * 当AI服务不可用时，提供基础的统计分析功能
 */
@Slf4j
@Service
public class FallbackAIService {
    
    /**
     * 生成基础汇总报告
     */
    public AISummaryResult generateBasicSummary(Long teamId, LocalDateTime startDate, LocalDateTime endDate, List<Issue> issues) {
        log.info("使用降级服务生成基础汇总报告: teamId={}, issueCount={}", teamId, issues.size());
        
        StringBuilder summary = new StringBuilder();
        
        // 基本统计信息
        summary.append("# 代码评审问题汇总报告\n\n");
        summary.append("**报告时间范围**: ").append(formatDate(startDate)).append(" 至 ").append(formatDate(endDate)).append("\n\n");
        
        // 总体概况
        summary.append("## 总体概况\n\n");
        summary.append("- **问题总数**: ").append(issues.size()).append("\n");
        
        Map<Issue.IssueStatus, Long> statusStats = issues.stream()
                .collect(Collectors.groupingBy(Issue::getStatus, Collectors.counting()));
        
        summary.append("- **待处理**: ").append(statusStats.getOrDefault(Issue.IssueStatus.OPEN, 0L)).append("\n");
        summary.append("- **处理中**: ").append(statusStats.getOrDefault(Issue.IssueStatus.IN_PROGRESS, 0L)).append("\n");
        summary.append("- **已解决**: ").append(statusStats.getOrDefault(Issue.IssueStatus.RESOLVED, 0L)).append("\n");
        summary.append("- **已关闭**: ").append(statusStats.getOrDefault(Issue.IssueStatus.CLOSED, 0L)).append("\n\n");
        
        // 问题类型分布
        summary.append("## 问题类型分布\n\n");
        Map<IssueType, Long> typeStats = issues.stream()
                .collect(Collectors.groupingBy(Issue::getIssueType, Collectors.counting()));
        
        typeStats.entrySet().stream()
                .sorted(Map.Entry.<IssueType, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    String typeName = getIssueTypeName(entry.getKey());
                    double percentage = (double) entry.getValue() / issues.size() * 100;
                    summary.append("- **").append(typeName).append("**: ")
                           .append(entry.getValue()).append(" (")
                           .append(String.format("%.1f", percentage)).append("%)\n");
                });
        
        summary.append("\n");
        
        // 严重级别分布
        summary.append("## 严重级别分布\n\n");
        Map<Severity, Long> severityStats = issues.stream()
                .collect(Collectors.groupingBy(Issue::getSeverity, Collectors.counting()));
        
        severityStats.entrySet().stream()
                .sorted((e1, e2) -> getSeverityWeight(e1.getKey()) - getSeverityWeight(e2.getKey()))
                .forEach(entry -> {
                    String severityName = getSeverityName(entry.getKey());
                    double percentage = (double) entry.getValue() / issues.size() * 100;
                    summary.append("- **").append(severityName).append("**: ")
                           .append(entry.getValue()).append(" (")
                           .append(String.format("%.1f", percentage)).append("%)\n");
                });
        
        summary.append("\n");
        
        // 高频问题分析
        summary.append("## 高频问题分析\n\n");
        List<String> frequentIssues = identifyFrequentIssues(issues);
        if (frequentIssues.isEmpty()) {
            summary.append("暂未发现明显的高频问题模式。\n\n");
        } else {
            summary.append("发现以下高频问题模式：\n\n");
            frequentIssues.forEach(issue -> summary.append("- ").append(issue).append("\n"));
            summary.append("\n");
        }
        
        // 改进建议
        summary.append("## 改进建议\n\n");
        List<String> suggestions = generateBasicSuggestions(typeStats, severityStats, issues.size());
        suggestions.forEach(suggestion -> summary.append("- ").append(suggestion).append("\n"));
        
        summary.append("\n");
        
        // 趋势分析
        summary.append("## 趋势分析\n\n");
        String trendAnalysis = analyzeTrend(issues);
        summary.append(trendAnalysis);
        
        AISummaryResult result = new AISummaryResult();
        result.setSummary(summary.toString());
        result.setGeneratedBy("基础统计分析");
        result.setGeneratedAt(LocalDateTime.now());
        result.setIssueCount(issues.size());
        result.setAnalysisType("BASIC_STATISTICS");
        
        return result;
    }
    
    /**
     * 识别高频问题
     */
    private List<String> identifyFrequentIssues(List<Issue> issues) {
        List<String> frequentIssues = new ArrayList<>();
        
        // 分析问题标题中的关键词
        Map<String, Integer> keywordCount = new HashMap<>();
        
        for (Issue issue : issues) {
            String title = issue.getTitle().toLowerCase();
            
            // 检查常见问题关键词
            if (title.contains("空指针") || title.contains("nullpointer") || title.contains("npe")) {
                keywordCount.merge("空指针异常", 1, Integer::sum);
            }
            if (title.contains("内存") || title.contains("memory")) {
                keywordCount.merge("内存相关问题", 1, Integer::sum);
            }
            if (title.contains("性能") || title.contains("慢") || title.contains("超时")) {
                keywordCount.merge("性能问题", 1, Integer::sum);
            }
            if (title.contains("安全") || title.contains("漏洞") || title.contains("注入")) {
                keywordCount.merge("安全问题", 1, Integer::sum);
            }
            if (title.contains("命名") || title.contains("注释") || title.contains("格式")) {
                keywordCount.merge("代码规范问题", 1, Integer::sum);
            }
        }
        
        // 找出出现频率较高的问题（至少出现3次且占比超过10%）
        int threshold = Math.max(3, issues.size() / 10);
        keywordCount.entrySet().stream()
                .filter(entry -> entry.getValue() >= threshold)
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> {
                    double percentage = (double) entry.getValue() / issues.size() * 100;
                    frequentIssues.add(String.format("%s (%d次, %.1f%%)", 
                            entry.getKey(), entry.getValue(), percentage));
                });
        
        return frequentIssues;
    }
    
    /**
     * 生成基础改进建议
     */
    private List<String> generateBasicSuggestions(Map<IssueType, Long> typeStats, 
                                                 Map<Severity, Long> severityStats, 
                                                 int totalIssues) {
        List<String> suggestions = new ArrayList<>();
        
        // 基于问题类型的建议
        IssueType mostCommonType = typeStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        
        if (mostCommonType != null) {
            long count = typeStats.get(mostCommonType);
            double percentage = (double) count / totalIssues * 100;
            
            if (percentage > 40) {
                switch (mostCommonType) {
                    case FUNCTIONAL_DEFECT:
                        suggestions.add("功能缺陷占比较高，建议加强单元测试和集成测试覆盖率");
                        suggestions.add("考虑引入代码审查检查清单，重点关注边界条件和异常处理");
                        break;
                    case PERFORMANCE_ISSUE:
                        suggestions.add("性能问题较多，建议建立性能测试基准和监控机制");
                        suggestions.add("考虑进行代码性能分析，识别热点代码路径");
                        break;
                    case SECURITY_VULNERABILITY:
                        suggestions.add("安全问题需要重点关注，建议进行安全代码审查培训");
                        suggestions.add("考虑集成静态安全分析工具到CI/CD流程");
                        break;
                    case CODE_STANDARD:
                        suggestions.add("代码规范问题较多，建议配置代码格式化工具和Lint规则");
                        suggestions.add("考虑在IDE中集成代码规范检查插件");
                        break;
                    case DESIGN_ISSUE:
                        suggestions.add("设计问题较多，建议加强架构设计评审和设计模式培训");
                        suggestions.add("考虑建立设计文档模板和评审流程");
                        break;
                }
            }
        }
        
        // 基于严重级别的建议
        long criticalCount = severityStats.getOrDefault(Severity.CRITICAL, 0L);
        if (criticalCount > 0) {
            double criticalPercentage = (double) criticalCount / totalIssues * 100;
            if (criticalPercentage > 20) {
                suggestions.add("严重问题占比较高，建议建立问题分级处理机制，优先解决严重问题");
            }
        }
        
        // 通用建议
        if (totalIssues > 50) {
            suggestions.add("问题数量较多，建议定期进行代码质量回顾会议");
        }
        
        suggestions.add("建议建立问题知识库，记录常见问题的解决方案");
        suggestions.add("考虑开展代码质量培训，提升团队整体代码质量意识");
        
        return suggestions;
    }
    
    /**
     * 分析趋势
     */
    private String analyzeTrend(List<Issue> issues) {
        if (issues.size() < 10) {
            return "数据量较少，暂无明显趋势。";
        }
        
        // 按创建时间排序
        issues.sort(Comparator.comparing(Issue::getCreatedAt));
        
        // 分析前半部分和后半部分的问题数量变化
        int midPoint = issues.size() / 2;
        List<Issue> firstHalf = issues.subList(0, midPoint);
        List<Issue> secondHalf = issues.subList(midPoint, issues.size());
        
        // 计算严重问题比例变化
        long firstHalfCritical = firstHalf.stream()
                .filter(i -> i.getSeverity() == Severity.CRITICAL || i.getSeverity() == Severity.MAJOR)
                .count();
        long secondHalfCritical = secondHalf.stream()
                .filter(i -> i.getSeverity() == Severity.CRITICAL || i.getSeverity() == Severity.MAJOR)
                .count();
        
        double firstHalfCriticalRate = (double) firstHalfCritical / firstHalf.size() * 100;
        double secondHalfCriticalRate = (double) secondHalfCritical / secondHalf.size() * 100;
        
        StringBuilder trend = new StringBuilder();
        
        if (secondHalfCriticalRate > firstHalfCriticalRate + 10) {
            trend.append("严重问题比例呈上升趋势，需要重点关注。");
        } else if (firstHalfCriticalRate > secondHalfCriticalRate + 10) {
            trend.append("严重问题比例呈下降趋势，代码质量有所改善。");
        } else {
            trend.append("问题严重程度相对稳定。");
        }
        
        return trend.toString();
    }
    
    // 辅助方法
    private String getIssueTypeName(IssueType type) {
        switch (type) {
            case FUNCTIONAL_DEFECT: return "功能缺陷";
            case PERFORMANCE_ISSUE: return "性能问题";
            case SECURITY_VULNERABILITY: return "安全漏洞";
            case CODE_STANDARD: return "代码规范";
            case DESIGN_ISSUE: return "设计问题";
            default: return type.name();
        }
    }
    
    private String getSeverityName(Severity severity) {
        switch (severity) {
            case CRITICAL: return "严重";
            case MAJOR: return "重要";
            case MINOR: return "一般";
            case SUGGESTION: return "建议";
            default: return severity.name();
        }
    }
    
    private int getSeverityWeight(Severity severity) {
        switch (severity) {
            case CRITICAL: return 1;
            case MAJOR: return 2;
            case MINOR: return 3;
            case SUGGESTION: return 4;
            default: return 5;
        }
    }
    
    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "未知";
        return dateTime.toLocalDate().toString();
    }
    
    /**
     * AI汇总结果
     */
    public static class AISummaryResult {
        private String summary;
        private String generatedBy;
        private LocalDateTime generatedAt;
        private int issueCount;
        private String analysisType;
        
        // Getters and Setters
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        
        public String getGeneratedBy() { return generatedBy; }
        public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
        
        public LocalDateTime getGeneratedAt() { return generatedAt; }
        public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
        
        public int getIssueCount() { return issueCount; }
        public void setIssueCount(int issueCount) { this.issueCount = issueCount; }
        
        public String getAnalysisType() { return analysisType; }
        public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    }
}