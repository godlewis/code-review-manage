package com.company.codereview.user.service;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.entity.Issue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 问题分类服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueClassificationService {
    
    // 功能缺陷关键词
    private static final Set<String> FUNCTIONAL_DEFECT_KEYWORDS = Set.of(
            "空指针", "nullpointer", "npe", "异常", "exception", "错误", "error",
            "崩溃", "crash", "失败", "fail", "bug", "缺陷", "defect", "资源泄露",
            "内存泄露", "死锁", "deadlock", "数组越界", "索引", "index"
    );
    
    // 性能问题关键词
    private static final Set<String> PERFORMANCE_KEYWORDS = Set.of(
            "性能", "performance", "慢", "slow", "超时", "timeout", "延迟", "delay",
            "响应时间", "response time", "吞吐量", "throughput", "内存", "memory",
            "cpu", "循环", "loop", "查询", "query", "n+1", "缓存", "cache"
    );
    
    // 安全漏洞关键词
    private static final Set<String> SECURITY_KEYWORDS = Set.of(
            "安全", "security", "漏洞", "vulnerability", "注入", "injection",
            "xss", "csrf", "sql注入", "权限", "permission", "认证", "authentication",
            "授权", "authorization", "密码", "password", "加密", "encryption"
    );
    
    // 代码规范关键词
    private static final Set<String> CODE_STANDARD_KEYWORDS = Set.of(
            "命名", "naming", "注释", "comment", "格式", "format", "缩进", "indent",
            "规范", "standard", "风格", "style", "可读性", "readability", "文档", "document"
    );
    
    // 设计问题关键词
    private static final Set<String> DESIGN_KEYWORDS = Set.of(
            "设计", "design", "架构", "architecture", "耦合", "coupling", "内聚", "cohesion",
            "单一职责", "srp", "开闭原则", "ocp", "依赖", "dependency", "接口", "interface",
            "抽象", "abstract", "继承", "inheritance", "多态", "polymorphism"
    );
    
    // 严重级别关键词
    private static final Set<String> CRITICAL_KEYWORDS = Set.of(
            "严重", "critical", "致命", "fatal", "崩溃", "crash", "数据丢失", "data loss",
            "安全漏洞", "security vulnerability", "系统宕机", "system down"
    );
    
    private static final Set<String> MAJOR_KEYWORDS = Set.of(
            "重要", "major", "影响功能", "功能异常", "性能问题", "performance issue",
            "用户体验", "user experience", "业务逻辑", "business logic"
    );
    
    private static final Set<String> MINOR_KEYWORDS = Set.of(
            "轻微", "minor", "小问题", "优化", "optimization", "改进", "improvement",
            "代码规范", "code standard", "格式", "format"
    );
    
    /**
     * 自动分类问题类型
     */
    public IssueType classifyIssueType(String title, String description) {
        log.debug("自动分类问题类型: title={}", title);
        
        String content = (title + " " + description).toLowerCase();
        
        // 计算各类型的匹配分数
        int functionalScore = calculateKeywordScore(content, FUNCTIONAL_DEFECT_KEYWORDS);
        int performanceScore = calculateKeywordScore(content, PERFORMANCE_KEYWORDS);
        int securityScore = calculateKeywordScore(content, SECURITY_KEYWORDS);
        int standardScore = calculateKeywordScore(content, CODE_STANDARD_KEYWORDS);
        int designScore = calculateKeywordScore(content, DESIGN_KEYWORDS);
        
        // 找出最高分数的类型
        int maxScore = Math.max(functionalScore, 
                       Math.max(performanceScore, 
                       Math.max(securityScore, 
                       Math.max(standardScore, designScore))));
        
        if (maxScore == 0) {
            return IssueType.FUNCTIONAL_DEFECT; // 默认类型
        }
        
        if (functionalScore == maxScore) {
            return IssueType.FUNCTIONAL_DEFECT;
        } else if (performanceScore == maxScore) {
            return IssueType.PERFORMANCE_ISSUE;
        } else if (securityScore == maxScore) {
            return IssueType.SECURITY_VULNERABILITY;
        } else if (standardScore == maxScore) {
            return IssueType.CODE_STANDARD;
        } else {
            return IssueType.DESIGN_ISSUE;
        }
    }
    
    /**
     * 自动评估严重级别
     */
    public Severity classifySeverity(String title, String description, IssueType issueType) {
        log.debug("自动评估严重级别: title={}, type={}", title, issueType);
        
        String content = (title + " " + description).toLowerCase();
        
        // 计算严重级别分数
        int criticalScore = calculateKeywordScore(content, CRITICAL_KEYWORDS);
        int majorScore = calculateKeywordScore(content, MAJOR_KEYWORDS);
        int minorScore = calculateKeywordScore(content, MINOR_KEYWORDS);
        
        // 根据问题类型调整基础严重级别
        Severity baseSeverity = getBaseSeverityByType(issueType);
        
        // 根据关键词分数调整严重级别
        if (criticalScore > 0) {
            return Severity.CRITICAL;
        } else if (majorScore > 0) {
            return baseSeverity == Severity.SUGGESTION ? Severity.MINOR : 
                   baseSeverity == Severity.MINOR ? Severity.MAJOR : baseSeverity;
        } else if (minorScore > 0) {
            return baseSeverity == Severity.CRITICAL ? Severity.MAJOR :
                   baseSeverity == Severity.MAJOR ? Severity.MINOR : baseSeverity;
        }
        
        return baseSeverity;
    }
    
    /**
     * 根据问题类型获取基础严重级别
     */
    private Severity getBaseSeverityByType(IssueType issueType) {
        switch (issueType) {
            case FUNCTIONAL_DEFECT:
                return Severity.MAJOR;
            case PERFORMANCE_ISSUE:
                return Severity.MAJOR;
            case SECURITY_VULNERABILITY:
                return Severity.CRITICAL;
            case CODE_STANDARD:
                return Severity.MINOR;
            case DESIGN_ISSUE:
                return Severity.MAJOR;
            default:
                return Severity.MINOR;
        }
    }
    
    /**
     * 计算关键词匹配分数
     */
    private int calculateKeywordScore(String content, Set<String> keywords) {
        int score = 0;
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                score++;
            }
        }
        return score;
    }
    
    /**
     * 智能分类问题
     */
    public Issue classifyIssue(Issue issue) {
        log.info("智能分类问题: issueId={}, title={}", issue.getId(), issue.getTitle());
        
        // 自动分类问题类型
        if (issue.getIssueType() == null) {
            IssueType classifiedType = classifyIssueType(issue.getTitle(), issue.getDescription());
            issue.setIssueType(classifiedType);
            log.debug("自动分类问题类型: issueId={}, type={}", issue.getId(), classifiedType);
        }
        
        // 自动评估严重级别
        if (issue.getSeverity() == null) {
            Severity classifiedSeverity = classifySeverity(issue.getTitle(), issue.getDescription(), issue.getIssueType());
            issue.setSeverity(classifiedSeverity);
            log.debug("自动评估严重级别: issueId={}, severity={}", issue.getId(), classifiedSeverity);
        }
        
        // 生成标签
        Set<String> tags = generateTags(issue);
        if (!tags.isEmpty()) {
            // 这里可以将标签存储到数据库或其他地方
            log.debug("生成问题标签: issueId={}, tags={}", issue.getId(), tags);
        }
        
        return issue;
    }
    
    /**
     * 生成问题标签
     */
    public Set<String> generateTags(Issue issue) {
        Set<String> tags = new HashSet<>();
        
        String content = (issue.getTitle() + " " + issue.getDescription()).toLowerCase();
        
        // 根据内容生成标签
        if (content.contains("空指针") || content.contains("nullpointer")) {
            tags.add("空指针异常");
        }
        
        if (content.contains("性能") || content.contains("慢") || content.contains("超时")) {
            tags.add("性能问题");
        }
        
        if (content.contains("安全") || content.contains("漏洞") || content.contains("注入")) {
            tags.add("安全风险");
        }
        
        if (content.contains("命名") || content.contains("注释") || content.contains("格式")) {
            tags.add("代码规范");
        }
        
        if (content.contains("设计") || content.contains("架构") || content.contains("耦合")) {
            tags.add("设计问题");
        }
        
        // 根据严重级别添加标签
        if (issue.getSeverity() == Severity.CRITICAL) {
            tags.add("紧急");
        } else if (issue.getSeverity() == Severity.MAJOR) {
            tags.add("重要");
        }
        
        // 根据问题类型添加标签
        tags.add(issue.getIssueType().name().toLowerCase().replace("_", "-"));
        
        return tags;
    }
    
    /**
     * 批量分类问题
     */
    public List<Issue> batchClassifyIssues(List<Issue> issues) {
        log.info("批量分类问题: count={}", issues.size());
        
        List<Issue> classifiedIssues = new ArrayList<>();
        
        for (Issue issue : issues) {
            try {
                Issue classifiedIssue = classifyIssue(issue);
                classifiedIssues.add(classifiedIssue);
            } catch (Exception e) {
                log.error("分类问题失败: issueId={}, error={}", issue.getId(), e.getMessage(), e);
                classifiedIssues.add(issue); // 分类失败时返回原问题
            }
        }
        
        log.info("批量分类完成: 成功分类{}个问题", classifiedIssues.size());
        return classifiedIssues;
    }
    
    /**
     * 获取问题分类建议
     */
    public IssueClassificationSuggestion getClassificationSuggestion(String title, String description) {
        log.debug("获取问题分类建议: title={}", title);
        
        IssueType suggestedType = classifyIssueType(title, description);
        Severity suggestedSeverity = classifySeverity(title, description, suggestedType);
        
        // 计算置信度
        String content = (title + " " + description).toLowerCase();
        double confidence = calculateClassificationConfidence(content, suggestedType);
        
        IssueClassificationSuggestion suggestion = new IssueClassificationSuggestion();
        suggestion.setSuggestedType(suggestedType);
        suggestion.setSuggestedSeverity(suggestedSeverity);
        suggestion.setConfidence(confidence);
        suggestion.setReason(generateClassificationReason(content, suggestedType, suggestedSeverity));
        
        return suggestion;
    }
    
    /**
     * 计算分类置信度
     */
    private double calculateClassificationConfidence(String content, IssueType suggestedType) {
        Set<String> keywords;
        switch (suggestedType) {
            case FUNCTIONAL_DEFECT:
                keywords = FUNCTIONAL_DEFECT_KEYWORDS;
                break;
            case PERFORMANCE_ISSUE:
                keywords = PERFORMANCE_KEYWORDS;
                break;
            case SECURITY_VULNERABILITY:
                keywords = SECURITY_KEYWORDS;
                break;
            case CODE_STANDARD:
                keywords = CODE_STANDARD_KEYWORDS;
                break;
            case DESIGN_ISSUE:
                keywords = DESIGN_KEYWORDS;
                break;
            default:
                return 0.5;
        }
        
        int matchCount = calculateKeywordScore(content, keywords);
        int totalKeywords = keywords.size();
        
        // 简单的置信度计算：匹配关键词数量 / 总关键词数量
        double confidence = Math.min(1.0, (double) matchCount / Math.min(5, totalKeywords));
        return Math.max(0.3, confidence); // 最低置信度为30%
    }
    
    /**
     * 生成分类原因说明
     */
    private String generateClassificationReason(String content, IssueType type, Severity severity) {
        StringBuilder reason = new StringBuilder();
        
        reason.append("基于内容分析，建议分类为").append(type.name()).append("，");
        reason.append("严重级别为").append(severity.name()).append("。");
        
        // 添加具体的匹配关键词
        Set<String> keywords = getKeywordsByType(type);
        List<String> matchedKeywords = new ArrayList<>();
        
        for (String keyword : keywords) {
            if (content.contains(keyword)) {
                matchedKeywords.add(keyword);
                if (matchedKeywords.size() >= 3) break; // 最多显示3个关键词
            }
        }
        
        if (!matchedKeywords.isEmpty()) {
            reason.append(" 匹配关键词：").append(String.join(", ", matchedKeywords));
        }
        
        return reason.toString();
    }
    
    /**
     * 根据类型获取关键词集合
     */
    private Set<String> getKeywordsByType(IssueType type) {
        switch (type) {
            case FUNCTIONAL_DEFECT:
                return FUNCTIONAL_DEFECT_KEYWORDS;
            case PERFORMANCE_ISSUE:
                return PERFORMANCE_KEYWORDS;
            case SECURITY_VULNERABILITY:
                return SECURITY_KEYWORDS;
            case CODE_STANDARD:
                return CODE_STANDARD_KEYWORDS;
            case DESIGN_ISSUE:
                return DESIGN_KEYWORDS;
            default:
                return Collections.emptySet();
        }
    }
    
    /**
     * 问题分类建议类
     */
    public static class IssueClassificationSuggestion {
        private IssueType suggestedType;
        private Severity suggestedSeverity;
        private double confidence;
        private String reason;
        
        // Getters and Setters
        public IssueType getSuggestedType() { return suggestedType; }
        public void setSuggestedType(IssueType suggestedType) { this.suggestedType = suggestedType; }
        
        public Severity getSuggestedSeverity() { return suggestedSeverity; }
        public void setSuggestedSeverity(Severity suggestedSeverity) { this.suggestedSeverity = suggestedSeverity; }
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}