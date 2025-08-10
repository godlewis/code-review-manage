package com.company.codereview.user.util;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.entity.Issue;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 问题分析工具类
 * 提供各种分析算法和计算方法
 */
@UtilityClass
public class IssueAnalysisUtil {
    
    /**
     * 计算两个问题的相似度
     * 
     * @param issue1 问题1
     * @param issue2 问题2
     * @return 相似度（0-1之间）
     */
    public static double calculateSimilarity(Issue issue1, Issue issue2) {
        double typeSimilarity = issue1.getIssueType() == issue2.getIssueType() ? 1.0 : 0.0;
        double severitySimilarity = calculateSeveritySimilarity(issue1.getSeverity(), issue2.getSeverity());
        double textSimilarity = calculateTextSimilarity(issue1.getDescription(), issue2.getDescription());
        
        // 加权平均
        return typeSimilarity * 0.3 + severitySimilarity * 0.2 + textSimilarity * 0.5;
    }
    
    /**
     * 计算严重级别相似度
     */
    private static double calculateSeveritySimilarity(Severity severity1, Severity severity2) {
        if (severity1 == severity2) {
            return 1.0;
        }
        
        int diff = Math.abs(severity1.getLevel() - severity2.getLevel());
        return Math.max(0.0, 1.0 - diff * 0.3);
    }
    
    /**
     * 计算文本相似度（基于Jaccard相似度）
     */
    private static double calculateTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }
        
        Set<String> words1 = extractWords(text1);
        Set<String> words2 = extractWords(text2);
        
        if (words1.isEmpty() && words2.isEmpty()) {
            return 1.0;
        }
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * 从文本中提取单词
     */
    private static Set<String> extractWords(String text) {
        return Arrays.stream(text.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5\\s]", " ")
                .split("\\s+"))
                .filter(word -> word.length() > 1)
                .collect(Collectors.toSet());
    }
    
    /**
     * 使用K-means算法对问题进行聚类
     * 
     * @param issues 问题列表
     * @param k 聚类数量
     * @return 聚类结果
     */
    public static Map<Integer, List<Issue>> kMeansCluster(List<Issue> issues, int k) {
        if (issues.size() <= k) {
            // 如果问题数量小于等于聚类数量，每个问题单独成一类
            Map<Integer, List<Issue>> result = new HashMap<>();
            for (int i = 0; i < issues.size(); i++) {
                result.put(i, Arrays.asList(issues.get(i)));
            }
            return result;
        }
        
        // 随机选择初始聚类中心
        List<Issue> centers = selectInitialCenters(issues, k);
        Map<Integer, List<Issue>> clusters = new HashMap<>();
        
        boolean converged = false;
        int maxIterations = 100;
        int iteration = 0;
        
        while (!converged && iteration < maxIterations) {
            // 清空聚类
            clusters.clear();
            for (int i = 0; i < k; i++) {
                clusters.put(i, new ArrayList<>());
            }
            
            // 将每个问题分配到最近的聚类中心
            for (Issue issue : issues) {
                int nearestCenter = findNearestCenter(issue, centers);
                clusters.get(nearestCenter).add(issue);
            }
            
            // 更新聚类中心
            List<Issue> newCenters = new ArrayList<>();
            for (int i = 0; i < k; i++) {
                List<Issue> cluster = clusters.get(i);
                if (!cluster.isEmpty()) {
                    newCenters.add(calculateClusterCenter(cluster));
                } else {
                    newCenters.add(centers.get(i)); // 保持原中心
                }
            }
            
            // 检查是否收敛
            converged = centersConverged(centers, newCenters);
            centers = newCenters;
            iteration++;
        }
        
        return clusters;
    }
    
    /**
     * 选择初始聚类中心
     */
    private static List<Issue> selectInitialCenters(List<Issue> issues, int k) {
        List<Issue> centers = new ArrayList<>();
        Random random = new Random();
        
        // 随机选择第一个中心
        centers.add(issues.get(random.nextInt(issues.size())));
        
        // 使用K-means++算法选择其余中心
        for (int i = 1; i < k; i++) {
            List<Double> distances = new ArrayList<>();
            double totalDistance = 0.0;
            
            for (Issue issue : issues) {
                double minDistance = Double.MAX_VALUE;
                for (Issue center : centers) {
                    double distance = 1.0 - calculateSimilarity(issue, center);
                    minDistance = Math.min(minDistance, distance);
                }
                distances.add(minDistance);
                totalDistance += minDistance;
            }
            
            // 根据距离权重随机选择下一个中心
            double randomValue = random.nextDouble() * totalDistance;
            double cumulativeDistance = 0.0;
            
            for (int j = 0; j < issues.size(); j++) {
                cumulativeDistance += distances.get(j);
                if (cumulativeDistance >= randomValue) {
                    centers.add(issues.get(j));
                    break;
                }
            }
        }
        
        return centers;
    }
    
    /**
     * 找到最近的聚类中心
     */
    private static int findNearestCenter(Issue issue, List<Issue> centers) {
        int nearestIndex = 0;
        double maxSimilarity = -1.0;
        
        for (int i = 0; i < centers.size(); i++) {
            double similarity = calculateSimilarity(issue, centers.get(i));
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                nearestIndex = i;
            }
        }
        
        return nearestIndex;
    }
    
    /**
     * 计算聚类中心
     */
    private static Issue calculateClusterCenter(List<Issue> cluster) {
        if (cluster.isEmpty()) {
            return null;
        }
        
        // 选择与聚类中所有问题平均相似度最高的问题作为中心
        Issue bestCenter = cluster.get(0);
        double bestAverageSimilarity = 0.0;
        
        for (Issue candidate : cluster) {
            double totalSimilarity = 0.0;
            for (Issue other : cluster) {
                if (!candidate.equals(other)) {
                    totalSimilarity += calculateSimilarity(candidate, other);
                }
            }
            double averageSimilarity = cluster.size() > 1 ? totalSimilarity / (cluster.size() - 1) : 1.0;
            
            if (averageSimilarity > bestAverageSimilarity) {
                bestAverageSimilarity = averageSimilarity;
                bestCenter = candidate;
            }
        }
        
        return bestCenter;
    }
    
    /**
     * 检查聚类中心是否收敛
     */
    private static boolean centersConverged(List<Issue> oldCenters, List<Issue> newCenters) {
        if (oldCenters.size() != newCenters.size()) {
            return false;
        }
        
        for (int i = 0; i < oldCenters.size(); i++) {
            if (!oldCenters.get(i).getId().equals(newCenters.get(i).getId())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 计算问题解决时间
     * 
     * @param issue 问题
     * @return 解决时间（小时），如果未解决返回-1
     */
    public static long calculateResolutionTime(Issue issue) {
        if (issue.getCreatedAt() == null || issue.getUpdatedAt() == null) {
            return -1;
        }
        
        if (issue.getStatus() != Issue.IssueStatus.RESOLVED && 
            issue.getStatus() != Issue.IssueStatus.CLOSED) {
            return -1;
        }
        
        return ChronoUnit.HOURS.between(issue.getCreatedAt(), issue.getUpdatedAt());
    }
    
    /**
     * 计算问题优先级评分
     * 
     * @param issue 问题
     * @return 优先级评分（0-100）
     */
    public static double calculatePriorityScore(Issue issue) {
        double severityScore = getSeverityScore(issue.getSeverity());
        double typeScore = getTypeScore(issue.getIssueType());
        double ageScore = getAgeScore(issue.getCreatedAt());
        
        // 加权计算优先级评分
        return severityScore * 0.5 + typeScore * 0.3 + ageScore * 0.2;
    }
    
    /**
     * 获取严重级别评分
     */
    private static double getSeverityScore(Severity severity) {
        switch (severity) {
            case CRITICAL: return 100.0;
            case MAJOR: return 70.0;
            case MINOR: return 40.0;
            case SUGGESTION: return 10.0;
            default: return 50.0;
        }
    }
    
    /**
     * 获取问题类型评分
     */
    private static double getTypeScore(IssueType type) {
        switch (type) {
            case SECURITY_VULNERABILITY: return 100.0;
            case FUNCTIONAL_DEFECT: return 80.0;
            case PERFORMANCE_ISSUE: return 60.0;
            case DESIGN_ISSUE: return 40.0;
            case CODE_STANDARD: return 20.0;
            default: return 50.0;
        }
    }
    
    /**
     * 获取问题年龄评分（问题存在时间越长，优先级越高）
     */
    private static double getAgeScore(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 0.0;
        }
        
        long daysOld = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        
        if (daysOld <= 1) return 10.0;
        if (daysOld <= 3) return 30.0;
        if (daysOld <= 7) return 50.0;
        if (daysOld <= 14) return 70.0;
        return 100.0;
    }
    
    /**
     * 计算团队问题解决效率
     * 
     * @param issues 问题列表
     * @return 效率指标
     */
    public static TeamEfficiencyMetrics calculateTeamEfficiency(List<Issue> issues) {
        if (issues.isEmpty()) {
            return TeamEfficiencyMetrics.builder()
                .averageResolutionTime(0.0)
                .resolutionRate(0.0)
                .criticalIssueResolutionTime(0.0)
                .overdueProbability(0.0)
                .build();
        }
        
        List<Long> resolutionTimes = issues.stream()
            .map(IssueAnalysisUtil::calculateResolutionTime)
            .filter(time -> time > 0)
            .collect(Collectors.toList());
        
        double averageResolutionTime = resolutionTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        double resolutionRate = (double) resolutionTimes.size() / issues.size() * 100;
        
        double criticalIssueResolutionTime = issues.stream()
            .filter(issue -> issue.getSeverity() == Severity.CRITICAL)
            .map(IssueAnalysisUtil::calculateResolutionTime)
            .filter(time -> time > 0)
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        long overdueCount = issues.stream()
            .filter(issue -> {
                if (issue.getCreatedAt() == null) return false;
                long daysOld = ChronoUnit.DAYS.between(issue.getCreatedAt(), LocalDateTime.now());
                return daysOld > 7 && (issue.getStatus() == Issue.IssueStatus.OPEN || 
                                     issue.getStatus() == Issue.IssueStatus.IN_PROGRESS);
            })
            .mapToLong(issue -> 1L)
            .sum();
        
        double overdueProbability = (double) overdueCount / issues.size() * 100;
        
        return TeamEfficiencyMetrics.builder()
            .averageResolutionTime(averageResolutionTime)
            .resolutionRate(resolutionRate)
            .criticalIssueResolutionTime(criticalIssueResolutionTime)
            .overdueProbability(overdueProbability)
            .build();
    }
    
    /**
     * 团队效率指标DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class TeamEfficiencyMetrics {
        private Double averageResolutionTime; // 平均解决时间（小时）
        private Double resolutionRate; // 解决率（百分比）
        private Double criticalIssueResolutionTime; // 严重问题平均解决时间
        private Double overdueProbability; // 逾期概率（百分比）
    }
}