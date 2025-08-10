package com.company.codereview.user.service;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.dto.IssueAnalysisResult;
import com.company.codereview.user.entity.Issue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 问题分析器测试类
 */
@ExtendWith(MockitoExtension.class)
class IssueAnalyzerTest {
    
    @InjectMocks
    private IssueAnalyzer issueAnalyzer;
    
    private List<Issue> testIssues;
    private LocalDate startDate;
    private LocalDate endDate;
    
    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().minusDays(7);
        endDate = LocalDate.now();
        
        // 创建测试数据
        testIssues = Arrays.asList(
            createTestIssue(1L, IssueType.FUNCTIONAL_DEFECT, Severity.CRITICAL, 
                "空指针异常导致系统崩溃", Issue.IssueStatus.RESOLVED),
            createTestIssue(2L, IssueType.PERFORMANCE_ISSUE, Severity.MAJOR, 
                "数据库查询性能慢", Issue.IssueStatus.OPEN),
            createTestIssue(3L, IssueType.CODE_STANDARD, Severity.MINOR, 
                "变量命名不规范", Issue.IssueStatus.RESOLVED),
            createTestIssue(4L, IssueType.SECURITY_VULNERABILITY, Severity.CRITICAL, 
                "SQL注入安全漏洞", Issue.IssueStatus.IN_PROGRESS),
            createTestIssue(5L, IssueType.FUNCTIONAL_DEFECT, Severity.MAJOR, 
                "空指针检查缺失", Issue.IssueStatus.OPEN),
            createTestIssue(6L, IssueType.DESIGN_ISSUE, Severity.MINOR, 
                "代码结构设计不合理", Issue.IssueStatus.CLOSED)
        );
    }
    
    @Test
    void testAnalyzeTeamIssues() {
        // 执行分析
        IssueAnalysisResult result = issueAnalyzer.analyzeTeamIssues(testIssues, startDate, endDate);
        
        // 验证基本统计信息
        assertNotNull(result);
        assertEquals(6L, result.getTotalIssues());
        assertEquals(3L, result.getResolvedIssues()); // RESOLVED + CLOSED
        assertEquals(50.0, result.getResolutionRate(), 0.01);
        
        // 验证类型分布
        assertNotNull(result.getTypeDistribution());
        assertEquals(2L, result.getTypeDistribution().get(IssueType.FUNCTIONAL_DEFECT));
        assertEquals(1L, result.getTypeDistribution().get(IssueType.PERFORMANCE_ISSUE));
        assertEquals(1L, result.getTypeDistribution().get(IssueType.CODE_STANDARD));
        assertEquals(1L, result.getTypeDistribution().get(IssueType.SECURITY_VULNERABILITY));
        assertEquals(1L, result.getTypeDistribution().get(IssueType.DESIGN_ISSUE));
        
        // 验证严重级别分布
        assertNotNull(result.getSeverityDistribution());
        assertEquals(2L, result.getSeverityDistribution().get(Severity.CRITICAL));
        assertEquals(2L, result.getSeverityDistribution().get(Severity.MAJOR));
        assertEquals(2L, result.getSeverityDistribution().get(Severity.MINOR));
        
        // 验证高频问题
        assertNotNull(result.getFrequentIssues());
        assertFalse(result.getFrequentIssues().isEmpty());
        
        // 验证趋势分析
        assertNotNull(result.getTrendAnalysis());
        assertNotNull(result.getTrendAnalysis().getOverallDirection());
        
        // 验证模式识别
        assertNotNull(result.getPatterns());
        
        // 验证聚类结果
        assertNotNull(result.getClusters());
    }
    
    @Test
    void testAnalyzeEmptyIssueList() {
        // 测试空问题列表
        IssueAnalysisResult result = issueAnalyzer.analyzeTeamIssues(Arrays.asList(), startDate, endDate);
        
        assertNotNull(result);
        assertEquals(0L, result.getTotalIssues());
        assertEquals(0L, result.getResolvedIssues());
        assertEquals(0.0, result.getResolutionRate());
        assertTrue(result.getTypeDistribution().isEmpty());
        assertTrue(result.getSeverityDistribution().isEmpty());
        assertTrue(result.getFrequentIssues().isEmpty());
        assertTrue(result.getPatterns().isEmpty());
        assertTrue(result.getClusters().isEmpty());
    }
    
    @Test
    void testFrequentIssueIdentification() {
        // 创建包含重复关键词的问题
        List<Issue> issuesWithDuplicates = Arrays.asList(
            createTestIssue(1L, IssueType.FUNCTIONAL_DEFECT, Severity.CRITICAL, 
                "空指针异常导致系统崩溃", Issue.IssueStatus.RESOLVED),
            createTestIssue(2L, IssueType.FUNCTIONAL_DEFECT, Severity.MAJOR, 
                "空指针检查缺失导致错误", Issue.IssueStatus.OPEN),
            createTestIssue(3L, IssueType.FUNCTIONAL_DEFECT, Severity.MINOR, 
                "空指针处理不当", Issue.IssueStatus.RESOLVED),
            createTestIssue(4L, IssueType.PERFORMANCE_ISSUE, Severity.MAJOR, 
                "性能问题导致响应慢", Issue.IssueStatus.OPEN),
            createTestIssue(5L, IssueType.PERFORMANCE_ISSUE, Severity.MINOR, 
                "性能优化需求", Issue.IssueStatus.IN_PROGRESS)
        );
        
        IssueAnalysisResult result = issueAnalyzer.analyzeTeamIssues(issuesWithDuplicates, startDate, endDate);
        
        assertNotNull(result.getFrequentIssues());
        assertFalse(result.getFrequentIssues().isEmpty());
        
        // 验证高频问题包含"空指针"和"性能"相关的问题
        boolean hasNullPointerIssue = result.getFrequentIssues().stream()
            .anyMatch(issue -> issue.getKeyword().contains("空指针") || issue.getKeyword().contains("null"));
        boolean hasPerformanceIssue = result.getFrequentIssues().stream()
            .anyMatch(issue -> issue.getKeyword().contains("性能") || issue.getKeyword().contains("performance"));
        
        assertTrue(hasNullPointerIssue || hasPerformanceIssue);
    }
    
    @Test
    void testPatternIdentification() {
        // 创建包含特定模式的问题
        List<Issue> patternsIssues = Arrays.asList(
            createTestIssue(1L, IssueType.FUNCTIONAL_DEFECT, Severity.CRITICAL, 
                "NullPointerException in user service", Issue.IssueStatus.OPEN),
            createTestIssue(2L, IssueType.FUNCTIONAL_DEFECT, Severity.MAJOR, 
                "空指针异常处理", Issue.IssueStatus.RESOLVED),
            createTestIssue(3L, IssueType.SECURITY_VULNERABILITY, Severity.CRITICAL, 
                "SQL注入安全漏洞", Issue.IssueStatus.OPEN),
            createTestIssue(4L, IssueType.SECURITY_VULNERABILITY, Severity.MAJOR, 
                "XSS攻击防护缺失", Issue.IssueStatus.IN_PROGRESS),
            createTestIssue(5L, IssueType.PERFORMANCE_ISSUE, Severity.MAJOR, 
                "数据库查询性能慢", Issue.IssueStatus.OPEN)
        );
        
        IssueAnalysisResult result = issueAnalyzer.analyzeTeamIssues(patternsIssues, startDate, endDate);
        
        assertNotNull(result.getPatterns());
        assertFalse(result.getPatterns().isEmpty());
        
        // 验证识别出的模式
        boolean hasNullPointerPattern = result.getPatterns().stream()
            .anyMatch(pattern -> "NULL_POINTER_PATTERN".equals(pattern.getPatternId()));
        boolean hasSecurityPattern = result.getPatterns().stream()
            .anyMatch(pattern -> "SECURITY_PATTERN".equals(pattern.getPatternId()));
        
        assertTrue(hasNullPointerPattern);
        assertTrue(hasSecurityPattern);
    }
    
    @Test
    void testClusterAnalysis() {
        IssueAnalysisResult result = issueAnalyzer.analyzeTeamIssues(testIssues, startDate, endDate);
        
        assertNotNull(result.getClusters());
        
        // 验证聚类结果
        for (var cluster : result.getClusters()) {
            assertNotNull(cluster.getClusterId());
            assertNotNull(cluster.getClusterName());
            assertNotNull(cluster.getDominantType());
            assertNotNull(cluster.getDominantSeverity());
            assertTrue(cluster.getIssueCount() >= 2); // 最小聚类大小
            assertNotNull(cluster.getCharacteristicKeywords());
            assertTrue(cluster.getSimilarity() >= 0.0 && cluster.getSimilarity() <= 1.0);
            assertTrue(cluster.getWeight() >= 0.0);
        }
    }
    
    @Test
    void testTrendAnalysis() {
        IssueAnalysisResult result = issueAnalyzer.analyzeTeamIssues(testIssues, startDate, endDate);
        
        assertNotNull(result.getTrendAnalysis());
        assertNotNull(result.getTrendAnalysis().getOverallDirection());
        assertNotNull(result.getTrendAnalysis().getChangeRate());
        assertNotNull(result.getTrendAnalysis().getPredictedIssues());
        assertNotNull(result.getTrendAnalysis().getSummary());
        
        // 验证趋势点数据
        assertNotNull(result.getTrendAnalysis().getIssueTrend());
        assertNotNull(result.getTrendAnalysis().getResolutionTrend());
        assertNotNull(result.getTrendAnalysis().getQualityTrend());
    }
    
    /**
     * 创建测试问题对象
     */
    private Issue createTestIssue(Long id, IssueType type, Severity severity, 
                                 String description, Issue.IssueStatus status) {
        Issue issue = new Issue();
        issue.setId(id);
        issue.setIssueType(type);
        issue.setSeverity(severity);
        issue.setTitle("Test Issue " + id);
        issue.setDescription(description);
        issue.setStatus(status);
        issue.setCreatedAt(LocalDateTime.now().minusDays(id));
        issue.setUpdatedAt(status == Issue.IssueStatus.RESOLVED || status == Issue.IssueStatus.CLOSED 
            ? LocalDateTime.now().minusDays(id - 1) : null);
        return issue;
    }
}