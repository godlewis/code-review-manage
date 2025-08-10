package com.company.codereview.user.service;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.dto.*;
import com.company.codereview.user.entity.Issue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Issue Analyzer Component
 * Implements issue classification, clustering, frequent issue identification and trend analysis
 */
@Slf4j
@Component
public class IssueAnalyzer {
    
    /**
     * Analyze team issues
     */
    public IssueAnalysisResult analyzeTeamIssues(List<Issue> issues, LocalDate startDate, LocalDate endDate) {
        log.info("Starting team issue analysis, issue count: {}, date range: {} - {}", issues.size(), startDate, endDate);
        
        // Calculate type distribution
        Map<IssueType, Long> typeDistribution = calculateTypeDistribution(issues);
        
        // Calculate severity distribution
        Map<Severity, Long> severityDistribution = calculateSeverityDistribution(issues);
        
        // Identify frequent issues
        List<FrequentIssue> frequentIssues = identifyFrequentIssues(issues);
        
        // Trend analysis
        TrendAnalysis trendAnalysis = analyzeTrend(issues, startDate, endDate);
        
        // Pattern identification
        List<IssuePattern> patterns = identifyPatterns(issues);
        
        // Issue clustering
        List<IssueCluster> clusters = clusterIssues(issues);
        
        // Calculate resolution rate
        long totalIssues = issues.size();
        long resolvedIssues = issues.stream()
            .mapToLong(issue -> Issue.IssueStatus.RESOLVED.equals(issue.getStatus()) || 
                              Issue.IssueStatus.CLOSED.equals(issue.getStatus()) ? 1 : 0)
            .sum();
        double resolutionRate = totalIssues > 0 ? (double) resolvedIssues / totalIssues * 100 : 0.0;
        
        return IssueAnalysisResult.builder()
            .typeDistribution(typeDistribution)
            .severityDistribution(severityDistribution)
            .frequentIssues(frequentIssues)
            .trendAnalysis(trendAnalysis)
            .patterns(patterns)
            .clusters(clusters)
            .startDate(startDate)
            .endDate(endDate)
            .totalIssues(totalIssues)
            .resolvedIssues(resolvedIssues)
            .resolutionRate(resolutionRate)
            .build();
    }
    
    /**
     * Calculate issue type distribution
     */
    private Map<IssueType, Long> calculateTypeDistribution(List<Issue> issues) {
        return issues.stream()
            .collect(Collectors.groupingBy(Issue::getIssueType, Collectors.counting()));
    }
    
    /**
     * Calculate severity distribution
     */
    private Map<Severity, Long> calculateSeverityDistribution(List<Issue> issues) {
        return issues.stream()
            .collect(Collectors.groupingBy(Issue::getSeverity, Collectors.counting()));
    }
    
    /**
     * Identify frequent issues
     */
    private List<FrequentIssue> identifyFrequentIssues(List<Issue> issues) {
        // Extract keywords from issue descriptions and calculate frequency
        Map<String, List<Issue>> keywordGroups = new HashMap<>();
        
        for (Issue issue : issues) {
            List<String> keywords = extractKeywords(issue.getDescription());
            for (String keyword : keywords) {
                keywordGroups.computeIfAbsent(keyword, k -> new ArrayList<>()).add(issue);
            }
        }
        
        return keywordGroups.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 2) // At least 2 occurrences
            .map(entry -> {
                String keyword = entry.getKey();
                List<Issue> relatedIssues = entry.getValue();
                
                // Calculate average severity
                Severity avgSeverity = calculateAverageSeverity(relatedIssues);
                
                // Get dominant issue type
                IssueType dominantType = relatedIssues.stream()
                    .collect(Collectors.groupingBy(Issue::getIssueType, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(IssueType.CODE_STANDARD);
                
                // Calculate impact percentage
                double impactPercentage = (double) relatedIssues.size() / issues.size() * 100;
                
                return FrequentIssue.builder()
                    .keyword(keyword)
                    .count((long) relatedIssues.size())
                    .issueType(dominantType)
                    .averageSeverity(avgSeverity)
                    .example(relatedIssues.get(0).getDescription())
                    .suggestedSolution(generateSuggestedSolution(keyword, dominantType))
                    .impactPercentage(impactPercentage)
                    .build();
            })
            .sorted((a, b) -> Long.compare(b.getCount(), a.getCount()))
            .limit(10) // Return top 10 frequent issues
            .collect(Collectors.toList());
    }
    
    /**
     * Extract keywords from description
     */
    private List<String> extractKeywords(String description) {
        if (description == null || description.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // Simple keyword extraction logic
        String[] words = description.toLowerCase()
            .replaceAll("[^a-zA-Z0-9\\s]", " ")
            .split("\\s+");
        
        Set<String> stopWords = Set.of("the", "is", "in", "and", "or", "but", "if", "because", "so", "a", "an", "to", "of", "for", "with", "on", "at", "by", "from", "as", "be", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can", "this", "that", "these", "those");
        
        return Arrays.stream(words)
            .filter(word -> word.length() > 2)
            .filter(word -> !stopWords.contains(word))
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate average severity
     */
    private Severity calculateAverageSeverity(List<Issue> issues) {
        double avgLevel = issues.stream()
            .mapToInt(issue -> issue.getSeverity().getLevel())
            .average()
            .orElse(3.0);
        
        // Return closest severity level
        if (avgLevel <= 1.5) return Severity.CRITICAL;
        if (avgLevel <= 2.5) return Severity.MAJOR;
        if (avgLevel <= 3.5) return Severity.MINOR;
        return Severity.SUGGESTION;
    }
    
    /**
     * Generate suggested solution
     */
    private String generateSuggestedSolution(String keyword, IssueType issueType) {
        Map<IssueType, String> solutionTemplates = Map.of(
            IssueType.FUNCTIONAL_DEFECT, "Strengthen unit test coverage, especially for " + keyword + " related functionality",
            IssueType.PERFORMANCE_ISSUE, "Optimize " + keyword + " related performance bottlenecks, consider caching or algorithm optimization",
            IssueType.SECURITY_VULNERABILITY, "Immediately fix " + keyword + " related security vulnerabilities, strengthen security review",
            IssueType.CODE_STANDARD, "Standardize " + keyword + " related code standards, use code formatting tools",
            IssueType.DESIGN_ISSUE, "Refactor " + keyword + " related design to improve code maintainability"
        );
        
        return solutionTemplates.getOrDefault(issueType, "Develop specialized improvement plan for " + keyword);
    }
    
    /**
     * Trend analysis
     */
    private TrendAnalysis analyzeTrend(List<Issue> issues, LocalDate startDate, LocalDate endDate) {
        // Calculate weekly issue trend
        List<TrendAnalysis.TrendPoint> issueTrend = calculateWeeklyIssueTrend(issues, startDate, endDate);
        
        // Calculate weekly resolution trend
        List<TrendAnalysis.TrendPoint> resolutionTrend = calculateWeeklyResolutionTrend(issues, startDate, endDate);
        
        // Calculate quality score trend
        List<TrendAnalysis.TrendPoint> qualityTrend = calculateWeeklyQualityTrend(issues, startDate, endDate);
        
        // Analyze overall trend direction
        TrendAnalysis.TrendDirection overallDirection = analyzeOverallTrend(issueTrend);
        
        // Calculate change rate
        double changeRate = calculateChangeRate(issueTrend);
        
        // Predict next period issues
        long predictedIssues = predictNextPeriodIssues(issueTrend);
        
        // Generate trend summary
        String summary = generateTrendSummary(overallDirection, changeRate, predictedIssues);
        
        return TrendAnalysis.builder()
            .issueTrend(issueTrend)
            .resolutionTrend(resolutionTrend)
            .qualityTrend(qualityTrend)
            .overallDirection(overallDirection)
            .changeRate(changeRate)
            .predictedIssues(predictedIssues)
            .summary(summary)
            .build();
    }
    
    /**
     * Calculate weekly issue trend
     */
    private List<TrendAnalysis.TrendPoint> calculateWeeklyIssueTrend(List<Issue> issues, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Long> weeklyCount = issues.stream()
            .filter(issue -> issue.getCreatedAt() != null)
            .collect(Collectors.groupingBy(
                issue -> issue.getCreatedAt().toLocalDate().with(java.time.DayOfWeek.MONDAY),
                Collectors.counting()
            ));
        
        List<TrendAnalysis.TrendPoint> trendPoints = new ArrayList<>();
        LocalDate current = startDate.with(java.time.DayOfWeek.MONDAY);
        
        while (!current.isAfter(endDate)) {
            Long count = weeklyCount.getOrDefault(current, 0L);
            trendPoints.add(TrendAnalysis.TrendPoint.builder()
                .date(current)
                .value(count.doubleValue())
                .label("Week " + current.format(java.time.format.DateTimeFormatter.ofPattern("w")))
                .build());
            current = current.plusWeeks(1);
        }
        
        return trendPoints;
    }
    
    /**
     * Calculate weekly resolution trend
     */
    private List<TrendAnalysis.TrendPoint> calculateWeeklyResolutionTrend(List<Issue> issues, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<Issue>> weeklyIssues = issues.stream()
            .filter(issue -> issue.getCreatedAt() != null)
            .collect(Collectors.groupingBy(
                issue -> issue.getCreatedAt().toLocalDate().with(java.time.DayOfWeek.MONDAY)
            ));
        
        List<TrendAnalysis.TrendPoint> trendPoints = new ArrayList<>();
        LocalDate current = startDate.with(java.time.DayOfWeek.MONDAY);
        
        while (!current.isAfter(endDate)) {
            List<Issue> weekIssues = weeklyIssues.getOrDefault(current, Collections.emptyList());
            double resolutionRate = 0.0;
            
            if (!weekIssues.isEmpty()) {
                long resolved = weekIssues.stream()
                    .mapToLong(issue -> Issue.IssueStatus.RESOLVED.equals(issue.getStatus()) || 
                                      Issue.IssueStatus.CLOSED.equals(issue.getStatus()) ? 1 : 0)
                    .sum();
                resolutionRate = (double) resolved / weekIssues.size() * 100;
            }
            
            trendPoints.add(TrendAnalysis.TrendPoint.builder()
                .date(current)
                .value(resolutionRate)
                .label("Week " + current.format(java.time.format.DateTimeFormatter.ofPattern("w")))
                .build());
            current = current.plusWeeks(1);
        }
        
        return trendPoints;
    }
    
    /**
     * Calculate weekly quality trend
     */
    private List<TrendAnalysis.TrendPoint> calculateWeeklyQualityTrend(List<Issue> issues, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<Issue>> weeklyIssues = issues.stream()
            .filter(issue -> issue.getCreatedAt() != null)
            .collect(Collectors.groupingBy(
                issue -> issue.getCreatedAt().toLocalDate().with(java.time.DayOfWeek.MONDAY)
            ));
        
        List<TrendAnalysis.TrendPoint> trendPoints = new ArrayList<>();
        LocalDate current = startDate.with(java.time.DayOfWeek.MONDAY);
        
        while (!current.isAfter(endDate)) {
            List<Issue> weekIssues = weeklyIssues.getOrDefault(current, Collections.emptyList());
            double qualityScore = calculateQualityScore(weekIssues);
            
            trendPoints.add(TrendAnalysis.TrendPoint.builder()
                .date(current)
                .value(qualityScore)
                .label("Week " + current.format(java.time.format.DateTimeFormatter.ofPattern("w")))
                .build());
            current = current.plusWeeks(1);
        }
        
        return trendPoints;
    }
    
    /**
     * Calculate quality score based on issue severity
     */
    private double calculateQualityScore(List<Issue> issues) {
        if (issues.isEmpty()) {
            return 100.0; // Perfect score when no issues
        }
        
        // Calculate deduction based on severity
        double totalDeduction = issues.stream()
            .mapToDouble(issue -> {
                switch (issue.getSeverity()) {
                    case CRITICAL: return 10.0;
                    case MAJOR: return 5.0;
                    case MINOR: return 2.0;
                    case SUGGESTION: return 0.5;
                    default: return 1.0;
                }
            })
            .sum();
        
        // Base score 100, deduct based on issue count and severity
        double score = Math.max(0, 100 - totalDeduction);
        return Math.round(score * 100.0) / 100.0;
    }
    
    /**
     * Analyze overall trend direction
     */
    private TrendAnalysis.TrendDirection analyzeOverallTrend(List<TrendAnalysis.TrendPoint> trendPoints) {
        if (trendPoints.size() < 2) {
            return TrendAnalysis.TrendDirection.UNKNOWN;
        }
        
        // Calculate linear regression slope
        double slope = calculateTrendSlope(trendPoints);
        
        if (slope < -0.1) {
            return TrendAnalysis.TrendDirection.IMPROVING; // Decreasing issues means improving
        } else if (slope > 0.1) {
            return TrendAnalysis.TrendDirection.DECLINING; // Increasing issues means declining
        } else {
            return TrendAnalysis.TrendDirection.STABLE;
        }
    }
    
    /**
     * Calculate trend slope
     */
    private double calculateTrendSlope(List<TrendAnalysis.TrendPoint> trendPoints) {
        int n = trendPoints.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = trendPoints.get(i).getValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        
        return (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
    }
    
    /**
     * Calculate change rate
     */
    private double calculateChangeRate(List<TrendAnalysis.TrendPoint> trendPoints) {
        if (trendPoints.size() < 2) {
            return 0.0;
        }
        
        double firstValue = trendPoints.get(0).getValue();
        double lastValue = trendPoints.get(trendPoints.size() - 1).getValue();
        
        if (firstValue == 0) {
            return lastValue > 0 ? 100.0 : 0.0;
        }
        
        return ((lastValue - firstValue) / firstValue) * 100;
    }
    
    /**
     * Predict next period issues
     */
    private long predictNextPeriodIssues(List<TrendAnalysis.TrendPoint> trendPoints) {
        if (trendPoints.size() < 2) {
            return 0L;
        }
        
        // Use simple linear prediction
        double slope = calculateTrendSlope(trendPoints);
        double lastValue = trendPoints.get(trendPoints.size() - 1).getValue();
        double predicted = lastValue + slope;
        
        return Math.max(0L, Math.round(predicted));
    }
    
    /**
     * Generate trend summary
     */
    private String generateTrendSummary(TrendAnalysis.TrendDirection direction, double changeRate, long predictedIssues) {
        StringBuilder summary = new StringBuilder();
        
        switch (direction) {
            case IMPROVING:
                summary.append("Code quality shows improving trend, ");
                break;
            case DECLINING:
                summary.append("Code quality shows declining trend, needs attention, ");
                break;
            case STABLE:
                summary.append("Code quality remains stable, ");
                break;
            default:
                summary.append("Trend is unclear, ");
        }
        
        summary.append(String.format("change rate is %.1f%%, ", changeRate));
        summary.append(String.format("predicted next period issues: %d.", predictedIssues));
        
        if (Math.abs(changeRate) > 20) {
            summary.append(" Recommend focusing on root causes of changes.");
        }
        
        return summary.toString();
    }
    
    /**
     * Identify issue patterns
     */
    private List<IssuePattern> identifyPatterns(List<Issue> issues) {
        List<IssuePattern> patterns = new ArrayList<>();
        
        // Pattern 1: Null pointer exception pattern
        identifyNullPointerPattern(issues, patterns);
        
        // Pattern 2: Performance issue pattern
        identifyPerformancePattern(issues, patterns);
        
        // Pattern 3: Security vulnerability pattern
        identifySecurityPattern(issues, patterns);
        
        // Pattern 4: Code standard pattern
        identifyCodeStandardPattern(issues, patterns);
        
        return patterns.stream()
            .filter(pattern -> pattern.getFrequency() >= 2) // At least 2 occurrences
            .sorted((a, b) -> Long.compare(b.getFrequency(), a.getFrequency()))
            .collect(Collectors.toList());
    }
    
    /**
     * Identify null pointer exception pattern
     */
    private void identifyNullPointerPattern(List<Issue> issues, List<IssuePattern> patterns) {
        List<String> nullPointerKeywords = Arrays.asList("null", "nullpointer", "npe", "pointer");
        
        List<Issue> matchedIssues = issues.stream()
            .filter(issue -> containsAnyKeyword(issue.getDescription(), nullPointerKeywords) ||
                           containsAnyKeyword(issue.getTitle(), nullPointerKeywords))
            .collect(Collectors.toList());
        
        if (!matchedIssues.isEmpty()) {
            patterns.add(IssuePattern.builder()
                .patternId("NULL_POINTER_PATTERN")
                .patternName("Null Pointer Exception Pattern")
                .description("Code contains potential null pointer exception risks")
                .matchedTypes(Arrays.asList(IssueType.FUNCTIONAL_DEFECT, IssueType.CODE_STANDARD))
                .frequency((long) matchedIssues.size())
                .confidence(0.85)
                .keywords(nullPointerKeywords)
                .examples(matchedIssues.stream().limit(3).map(Issue::getDescription).collect(Collectors.toList()))
                .preventionSuggestion("Recommend using Optional class or adding null checks")
                .affectedAreas(Arrays.asList("Business Logic Layer", "Data Access Layer"))
                .build());
        }
    }
    
    /**
     * Identify performance issue pattern
     */
    private void identifyPerformancePattern(List<Issue> issues, List<IssuePattern> patterns) {
        List<String> performanceKeywords = Arrays.asList("performance", "slow", "timeout", "memory", "cpu", "lag");
        
        List<Issue> matchedIssues = issues.stream()
            .filter(issue -> issue.getIssueType() == IssueType.PERFORMANCE_ISSUE ||
                           containsAnyKeyword(issue.getDescription(), performanceKeywords))
            .collect(Collectors.toList());
        
        if (!matchedIssues.isEmpty()) {
            patterns.add(IssuePattern.builder()
                .patternId("PERFORMANCE_PATTERN")
                .patternName("Performance Issue Pattern")
                .description("Code contains performance bottlenecks or improper resource usage")
                .matchedTypes(Arrays.asList(IssueType.PERFORMANCE_ISSUE))
                .frequency((long) matchedIssues.size())
                .confidence(0.90)
                .keywords(performanceKeywords)
                .examples(matchedIssues.stream().limit(3).map(Issue::getDescription).collect(Collectors.toList()))
                .preventionSuggestion("Recommend performance testing and code optimization")
                .affectedAreas(Arrays.asList("Database Queries", "Algorithm Implementation", "Cache Usage"))
                .build());
        }
    }
    
    /**
     * Identify security vulnerability pattern
     */
    private void identifySecurityPattern(List<Issue> issues, List<IssuePattern> patterns) {
        List<String> securityKeywords = Arrays.asList("security", "injection", "xss", "csrf", "auth", "encrypt");
        
        List<Issue> matchedIssues = issues.stream()
            .filter(issue -> issue.getIssueType() == IssueType.SECURITY_VULNERABILITY ||
                           containsAnyKeyword(issue.getDescription(), securityKeywords))
            .collect(Collectors.toList());
        
        if (!matchedIssues.isEmpty()) {
            patterns.add(IssuePattern.builder()
                .patternId("SECURITY_PATTERN")
                .patternName("Security Vulnerability Pattern")
                .description("Code contains security risks or vulnerabilities")
                .matchedTypes(Arrays.asList(IssueType.SECURITY_VULNERABILITY))
                .frequency((long) matchedIssues.size())
                .confidence(0.95)
                .keywords(securityKeywords)
                .examples(matchedIssues.stream().limit(3).map(Issue::getDescription).collect(Collectors.toList()))
                .preventionSuggestion("Recommend security review and vulnerability scanning")
                .affectedAreas(Arrays.asList("User Input Validation", "Access Control", "Data Transmission"))
                .build());
        }
    }
    
    /**
     * Identify code standard pattern
     */
    private void identifyCodeStandardPattern(List<Issue> issues, List<IssuePattern> patterns) {
        List<String> standardKeywords = Arrays.asList("naming", "format", "comment", "standard", "style");
        
        List<Issue> matchedIssues = issues.stream()
            .filter(issue -> issue.getIssueType() == IssueType.CODE_STANDARD ||
                           containsAnyKeyword(issue.getDescription(), standardKeywords))
            .collect(Collectors.toList());
        
        if (!matchedIssues.isEmpty()) {
            patterns.add(IssuePattern.builder()
                .patternId("CODE_STANDARD_PATTERN")
                .patternName("Code Standard Pattern")
                .description("Code does not conform to team standards or best practices")
                .matchedTypes(Arrays.asList(IssueType.CODE_STANDARD))
                .frequency((long) matchedIssues.size())
                .confidence(0.80)
                .keywords(standardKeywords)
                .examples(matchedIssues.stream().limit(3).map(Issue::getDescription).collect(Collectors.toList()))
                .preventionSuggestion("Recommend using code formatting tools and static analysis")
                .affectedAreas(Arrays.asList("Naming Convention", "Code Format", "Comment Standard"))
                .build());
        }
    }
    
    /**
     * Check if text contains any keyword
     */
    private boolean containsAnyKeyword(String text, List<String> keywords) {
        if (text == null) return false;
        String lowerText = text.toLowerCase();
        return keywords.stream().anyMatch(lowerText::contains);
    }
    
    /**
     * Issue clustering
     */
    private List<IssueCluster> clusterIssues(List<Issue> issues) {
        List<IssueCluster> clusters = new ArrayList<>();
        
        // Basic clustering by issue type
        Map<IssueType, List<Issue>> typeGroups = issues.stream()
            .collect(Collectors.groupingBy(Issue::getIssueType));
        
        for (Map.Entry<IssueType, List<Issue>> entry : typeGroups.entrySet()) {
            IssueType type = entry.getKey();
            List<Issue> typeIssues = entry.getValue();
            
            if (typeIssues.size() >= 2) { // At least 2 issues to form a cluster
                // Further cluster by severity within same type
                Map<Severity, List<Issue>> severityGroups = typeIssues.stream()
                    .collect(Collectors.groupingBy(Issue::getSeverity));
                
                for (Map.Entry<Severity, List<Issue>> severityEntry : severityGroups.entrySet()) {
                    Severity severity = severityEntry.getKey();
                    List<Issue> severityIssues = severityEntry.getValue();
                    
                    if (severityIssues.size() >= 2) {
                        String clusterId = type.name() + "_" + severity.name();
                        String clusterName = type.getDescription() + " - " + severity.getDescription();
                        
                        // Extract characteristic keywords
                        List<String> keywords = extractClusterKeywords(severityIssues);
                        
                        // Calculate cluster similarity
                        double similarity = calculateClusterSimilarity(severityIssues);
                        
                        // Calculate cluster weight
                        double weight = calculateClusterWeight(severityIssues, issues.size());
                        
                        clusters.add(IssueCluster.builder()
                            .clusterId(clusterId)
                            .clusterName(clusterName)
                            .centerDescription(generateClusterCenterDescription(severityIssues))
                            .issueCount((long) severityIssues.size())
                            .dominantType(type)
                            .dominantSeverity(severity)
                            .issueIds(severityIssues.stream().map(Issue::getId).collect(Collectors.toList()))
                            .characteristicKeywords(keywords)
                            .similarity(similarity)
                            .recommendation(generateClusterRecommendation(type, severity, severityIssues.size()))
                            .weight(weight)
                            .build());
                    }
                }
            }
        }
        
        return clusters.stream()
            .sorted((a, b) -> Double.compare(b.getWeight(), a.getWeight()))
            .collect(Collectors.toList());
    }
    
    /**
     * Extract cluster characteristic keywords
     */
    private List<String> extractClusterKeywords(List<Issue> issues) {
        Map<String, Integer> keywordCount = new HashMap<>();
        
        for (Issue issue : issues) {
            List<String> keywords = extractKeywords(issue.getDescription());
            for (String keyword : keywords) {
                keywordCount.put(keyword, keywordCount.getOrDefault(keyword, 0) + 1);
            }
        }
        
        return keywordCount.entrySet().stream()
            .filter(entry -> entry.getValue() >= 2) // At least 2 occurrences
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate cluster similarity
     */
    private double calculateClusterSimilarity(List<Issue> issues) {
        if (issues.size() < 2) return 1.0;
        
        // Calculate similarity based on keyword overlap
        List<Set<String>> keywordSets = issues.stream()
            .map(issue -> new HashSet<>(extractKeywords(issue.getDescription())))
            .collect(Collectors.toList());
        
        double totalSimilarity = 0.0;
        int comparisons = 0;
        
        for (int i = 0; i < keywordSets.size(); i++) {
            for (int j = i + 1; j < keywordSets.size(); j++) {
                Set<String> set1 = keywordSets.get(i);
                Set<String> set2 = keywordSets.get(j);
                
                Set<String> intersection = new HashSet<>(set1);
                intersection.retainAll(set2);
                
                Set<String> union = new HashSet<>(set1);
                union.addAll(set2);
                
                double similarity = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
                totalSimilarity += similarity;
                comparisons++;
            }
        }
        
        return comparisons > 0 ? totalSimilarity / comparisons : 0.0;
    }
    
    /**
     * Generate cluster center description
     */
    private String generateClusterCenterDescription(List<Issue> issues) {
        // Find most representative issue description
        Map<String, Integer> keywordCount = new HashMap<>();
        
        for (Issue issue : issues) {
            List<String> keywords = extractKeywords(issue.getDescription());
            for (String keyword : keywords) {
                keywordCount.put(keyword, keywordCount.getOrDefault(keyword, 0) + 1);
            }
        }
        
        List<String> topKeywords = keywordCount.entrySet().stream()
            .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
            .limit(3)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        return "Main topics: " + String.join(", ", topKeywords);
    }
    
    /**
     * Calculate cluster weight
     */
    private double calculateClusterWeight(List<Issue> clusterIssues, int totalIssues) {
        // Calculate weight based on quantity ratio and severity
        double quantityWeight = (double) clusterIssues.size() / totalIssues;
        
        double severityWeight = clusterIssues.stream()
            .mapToDouble(issue -> {
                switch (issue.getSeverity()) {
                    case CRITICAL: return 1.0;
                    case MAJOR: return 0.7;
                    case MINOR: return 0.4;
                    case SUGGESTION: return 0.1;
                    default: return 0.5;
                }
            })
            .average()
            .orElse(0.5);
        
        return (quantityWeight * 0.6 + severityWeight * 0.4) * 100;
    }
    
    /**
     * Generate cluster recommendation
     */
    private String generateClusterRecommendation(IssueType type, Severity severity, int count) {
        StringBuilder recommendation = new StringBuilder();
        
        recommendation.append(String.format("Found %d %s type %s issues, ", 
            count, type.getDescription(), severity.getDescription()));
        
        switch (type) {
            case FUNCTIONAL_DEFECT:
                recommendation.append("recommend strengthening unit tests and integration tests");
                break;
            case PERFORMANCE_ISSUE:
                recommendation.append("recommend performance analysis and optimization");
                break;
            case SECURITY_VULNERABILITY:
                recommendation.append("recommend immediate security review and fixes");
                break;
            case CODE_STANDARD:
                recommendation.append("recommend standardizing code format and using automated tools");
                break;
            case DESIGN_ISSUE:
                recommendation.append("recommend architectural refactoring and design optimization");
                break;
        }
        
        if (severity == Severity.CRITICAL) {
            recommendation.append(", priority: HIGH");
        }
        
        return recommendation.toString();
    }
}