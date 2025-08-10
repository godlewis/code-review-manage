package com.company.codereview.user.service;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.dto.*;
import com.company.codereview.user.entity.Issue;
import com.company.codereview.user.entity.ReviewRecord;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计分析服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final ReviewRecordRepository reviewRecordRepository;
    private final IssueRepository issueRepository;
    private final FixRecordRepository fixRecordRepository;
    private final ReviewAssignmentRepository reviewAssignmentRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    
    /**
     * 计算个人统计数据
     */
    @Cacheable(value = "personal-statistics", key = "#userId + '-' + #startDate + '-' + #endDate")
    public PersonalStatistics calculatePersonalStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        log.info("计算用户 {} 的个人统计数据，时间范围：{} 到 {}", userId, startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 评审完成率计算
        double completionRate = calculateReviewCompletionRate(userId, startDateTime, endDateTime);
        
        // 问题发现数量统计
        long issuesFound = issueRepository.countIssuesFoundByReviewer(userId, startDateTime, endDateTime);
        
        // 整改及时率计算
        double fixTimeliness = calculateFixTimeliness(userId, startDateTime, endDateTime);
        
        // 平均评审分数
        double averageReviewScore = calculateAverageReviewScore(userId, startDateTime, endDateTime);
        
        // 评审次数统计
        long totalReviews = reviewRecordRepository.countByReviewerId(userId, startDateTime, endDateTime);
        long completedReviews = countCompletedReviews(userId, startDateTime, endDateTime);
        
        // 问题统计
        long pendingIssues = countPendingIssues(userId);
        long resolvedIssues = countResolvedIssues(userId, startDateTime, endDateTime);
        
        // 个人成长趋势
        List<GrowthPoint> growthTrend = calculateGrowthTrend(userId, startDate, endDate);
        
        // 问题类型分布
        Map<String, Long> issueTypeDistribution = getIssueTypeDistribution(userId, startDateTime, endDateTime);
        
        // 严重级别分布
        Map<String, Long> severityDistribution = getSeverityDistribution(userId, startDateTime, endDateTime);
        
        // 月度统计数据
        List<MonthlyStatistics> monthlyStats = calculateMonthlyStatistics(userId, startDate, endDate);
        
        return PersonalStatistics.builder()
                .completionRate(completionRate)
                .issuesFound(issuesFound)
                .fixTimeliness(fixTimeliness)
                .averageReviewScore(averageReviewScore)
                .totalReviews(totalReviews)
                .completedReviews(completedReviews)
                .pendingIssues(pendingIssues)
                .resolvedIssues(resolvedIssues)
                .growthTrend(growthTrend)
                .issueTypeDistribution(issueTypeDistribution)
                .severityDistribution(severityDistribution)
                .monthlyStats(monthlyStats)
                .build();
    }
    
    /**
     * 计算团队统计数据
     */
    @Cacheable(value = "team-statistics", key = "#teamId + '-' + #startDate + '-' + #endDate")
    public TeamStatistics calculateTeamStatistics(Long teamId, LocalDate startDate, LocalDate endDate) {
        log.info("计算团队 {} 的统计数据，时间范围：{} 到 {}", teamId, startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 团队评审覆盖率
        double coverageRate = calculateTeamCoverageRate(teamId, startDateTime, endDateTime);
        
        // 团队平均评审分数
        double averageScore = calculateTeamAverageScore(teamId, startDateTime, endDateTime);
        
        // 问题统计
        long totalIssues = countTeamIssues(teamId, startDateTime, endDateTime);
        long resolvedIssues = countTeamResolvedIssues(teamId, startDateTime, endDateTime);
        double resolutionRate = totalIssues > 0 ? (double) resolvedIssues / totalIssues : 0.0;
        
        // 团队成员统计
        int memberCount = countTeamMembers(teamId);
        int activeMemberCount = countActiveTeamMembers(teamId, startDateTime, endDateTime);
        
        // 问题分布统计
        Map<String, Long> issueDistribution = getTeamIssueDistribution(teamId, startDateTime, endDateTime);
        
        // 严重级别分布
        Map<String, Long> severityDistribution = getTeamSeverityDistribution(teamId, startDateTime, endDateTime);
        
        // 代码质量趋势
        List<QualityPoint> qualityTrend = calculateQualityTrend(teamId, startDate, endDate);
        
        // 成员表现排名
        List<MemberPerformance> memberRankings = calculateMemberRankings(teamId, startDateTime, endDateTime);
        
        // 高频问题列表
        List<FrequentIssueStats> frequentIssues = getFrequentIssues(teamId, startDateTime, endDateTime);
        
        // 月度对比数据
        List<MonthlyComparison> monthlyComparisons = calculateMonthlyComparisons(teamId, startDate, endDate);
        
        return TeamStatistics.builder()
                .coverageRate(coverageRate)
                .averageScore(averageScore)
                .totalIssues(totalIssues)
                .resolvedIssues(resolvedIssues)
                .resolutionRate(resolutionRate)
                .memberCount(memberCount)
                .activeMemberCount(activeMemberCount)
                .issueDistribution(issueDistribution)
                .severityDistribution(severityDistribution)
                .qualityTrend(qualityTrend)
                .memberRankings(memberRankings)
                .frequentIssues(frequentIssues)
                .monthlyComparisons(monthlyComparisons)
                .build();
    }
    
    /**
     * 计算全局统计数据
     */
    @Cacheable(value = "global-statistics", key = "#startDate + '-' + #endDate")
    public GlobalStatistics calculateGlobalStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("计算全局统计数据，时间范围：{} 到 {}", startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        // 基础统计
        int totalTeams = countTotalTeams();
        int totalUsers = countTotalUsers();
        int activeUsers = countActiveUsers(startDateTime, endDateTime);
        
        // 评审和问题统计
        long totalReviews = countTotalReviews(startDateTime, endDateTime);
        long totalIssues = countTotalIssues(startDateTime, endDateTime);
        
        // 全局指标
        double globalResolutionRate = calculateGlobalResolutionRate(startDateTime, endDateTime);
        double globalAverageScore = calculateGlobalAverageScore(startDateTime, endDateTime);
        
        // 团队表现排名
        List<TeamPerformance> teamRankings = calculateTeamRankings(startDateTime, endDateTime);
        
        // 跨团队问题分布
        Map<String, Long> crossTeamIssueDistribution = getCrossTeamIssueDistribution(startDateTime, endDateTime);
        
        // 系统使用趋势
        List<UsageTrend> usageTrends = calculateUsageTrends(startDate, endDate);
        
        // 质量改进趋势
        List<QualityImprovementTrend> qualityTrends = calculateQualityImprovementTrends(startDate, endDate);
        
        // 最佳实践团队
        List<BestPracticeTeam> bestPracticeTeams = identifyBestPracticeTeams(startDateTime, endDateTime);
        
        return GlobalStatistics.builder()
                .totalTeams(totalTeams)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalReviews(totalReviews)
                .totalIssues(totalIssues)
                .globalResolutionRate(globalResolutionRate)
                .globalAverageScore(globalAverageScore)
                .teamRankings(teamRankings)
                .crossTeamIssueDistribution(crossTeamIssueDistribution)
                .usageTrends(usageTrends)
                .qualityTrends(qualityTrends)
                .bestPracticeTeams(bestPracticeTeams)
                .build();
    }
    
    // 私有辅助方法
    
    private double calculateReviewCompletionRate(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        int totalAssigned = reviewAssignmentRepository.countAssignedToUser(userId, startDate, endDate);
        int completed = reviewRecordRepository.countCompletedByReviewer(userId, startDate, endDate);
        return totalAssigned > 0 ? (double) completed / totalAssigned : 0.0;
    }
    
    private double calculateFixTimeliness(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return fixRecordRepository.calculateFixTimeliness(userId, startDate, endDate);
    }
    
    private double calculateAverageReviewScore(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRecordRepository.calculateAverageScore(userId, startDate, endDate);
    }
    
    private long countCompletedReviews(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRecordRepository.countCompletedByReviewer(userId, startDate, endDate);
    }
    
    private long countPendingIssues(Long userId) {
        return issueRepository.countPendingByAssignee(userId);
    }
    
    private long countResolvedIssues(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return issueRepository.countResolvedByAssignee(userId, startDate, endDate);
    }
    
    private List<GrowthPoint> calculateGrowthTrend(Long userId, LocalDate startDate, LocalDate endDate) {
        List<GrowthPoint> growthPoints = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            LocalDate weekEnd = current.plusDays(6);
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }
            
            LocalDateTime weekStart = current.atStartOfDay();
            LocalDateTime weekEndTime = weekEnd.atTime(23, 59, 59);
            
            double completionRate = calculateReviewCompletionRate(userId, weekStart, weekEndTime);
            long issuesFound = issueRepository.countIssuesFoundByReviewer(userId, weekStart, weekEndTime);
            double averageScore = calculateAverageReviewScore(userId, weekStart, weekEndTime);
            double fixTimeliness = calculateFixTimeliness(userId, weekStart, weekEndTime);
            
            growthPoints.add(GrowthPoint.builder()
                    .date(current)
                    .completionRate(completionRate)
                    .issuesFound(issuesFound)
                    .averageScore(averageScore)
                    .fixTimeliness(fixTimeliness)
                    .build());
            
            current = current.plusWeeks(1);
        }
        
        return growthPoints;
    }
    
    private Map<String, Long> getIssueTypeDistribution(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = issueRepository.getIssueTypeDistributionByReviewer(userId, startDate, endDate);
        return results.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("issue_type"),
                        map -> ((Number) map.get("count")).longValue()
                ));
    }
    
    private Map<String, Long> getSeverityDistribution(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = issueRepository.getSeverityDistributionByReviewer(userId, startDate, endDate);
        return results.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("severity"),
                        map -> ((Number) map.get("count")).longValue()
                ));
    }
    
    private List<MonthlyStatistics> calculateMonthlyStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        List<MonthlyStatistics> monthlyStats = new ArrayList<>();
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);
        
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            LocalDate monthStart = current.atDay(1);
            LocalDate monthEnd = current.atEndOfMonth();
            
            LocalDateTime monthStartTime = monthStart.atStartOfDay();
            LocalDateTime monthEndTime = monthEnd.atTime(23, 59, 59);
            
            long reviewCount = reviewRecordRepository.countByReviewerId(userId, monthStartTime, monthEndTime);
            long issueCount = issueRepository.countIssuesFoundByReviewer(userId, monthStartTime, monthEndTime);
            double averageScore = calculateAverageReviewScore(userId, monthStartTime, monthEndTime);
            double completionRate = calculateReviewCompletionRate(userId, monthStartTime, monthEndTime);
            double fixTimeliness = calculateFixTimeliness(userId, monthStartTime, monthEndTime);
            
            monthlyStats.add(MonthlyStatistics.builder()
                    .yearMonth(current)
                    .reviewCount(reviewCount)
                    .issueCount(issueCount)
                    .averageScore(averageScore)
                    .completionRate(completionRate)
                    .fixTimeliness(fixTimeliness)
                    .build());
            
            current = current.plusMonths(1);
        }
        
        return monthlyStats;
    }
    
    // 团队统计相关方法
    
    private double calculateTeamCoverageRate(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        int totalMembers = countTeamMembers(teamId);
        int membersWithReviews = reviewRecordRepository.countMembersWithReviews(teamId, startDate, endDate);
        return totalMembers > 0 ? (double) membersWithReviews / totalMembers : 0.0;
    }
    
    private double calculateTeamAverageScore(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRecordRepository.calculateTeamAverageScore(teamId, startDate, endDate);
    }
    
    private long countTeamIssues(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        return issueRepository.countByTeamId(teamId, startDate, endDate);
    }
    
    private long countTeamResolvedIssues(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        return issueRepository.countResolvedByTeamId(teamId, startDate, endDate);
    }
    
    private int countTeamMembers(Long teamId) {
        return userRepository.countByTeamId(teamId);
    }
    
    private int countActiveTeamMembers(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.countActiveByTeamId(teamId, startDate, endDate);
    }
    
    private Map<String, Long> getTeamIssueDistribution(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = issueRepository.getTeamIssueTypeDistribution(teamId, startDate, endDate);
        return results.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("issue_type"),
                        map -> ((Number) map.get("count")).longValue()
                ));
    }
    
    private Map<String, Long> getTeamSeverityDistribution(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = issueRepository.getTeamSeverityDistribution(teamId, startDate, endDate);
        return results.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("severity"),
                        map -> ((Number) map.get("count")).longValue()
                ));
    }
    
    private List<QualityPoint> calculateQualityTrend(Long teamId, LocalDate startDate, LocalDate endDate) {
        List<QualityPoint> qualityPoints = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            LocalDate weekEnd = current.plusDays(6);
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }
            
            LocalDateTime weekStart = current.atStartOfDay();
            LocalDateTime weekEndTime = weekEnd.atTime(23, 59, 59);
            
            double averageQualityScore = calculateTeamAverageScore(teamId, weekStart, weekEndTime);
            double issueDensity = calculateIssueDensity(teamId, weekStart, weekEndTime);
            double criticalIssueRatio = calculateCriticalIssueRatio(teamId, weekStart, weekEndTime);
            double resolutionRate = calculateTeamResolutionRate(teamId, weekStart, weekEndTime);
            
            qualityPoints.add(QualityPoint.builder()
                    .date(current)
                    .averageQualityScore(averageQualityScore)
                    .issueDensity(issueDensity)
                    .criticalIssueRatio(criticalIssueRatio)
                    .resolutionRate(resolutionRate)
                    .build());
            
            current = current.plusWeeks(1);
        }
        
        return qualityPoints;
    }
    
    private double calculateIssueDensity(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        long issueCount = countTeamIssues(teamId, startDate, endDate);
        long reviewCount = reviewRecordRepository.countByTeamId(teamId, startDate, endDate);
        return reviewCount > 0 ? (double) issueCount / reviewCount : 0.0;
    }
    
    private double calculateCriticalIssueRatio(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        long totalIssues = countTeamIssues(teamId, startDate, endDate);
        long criticalIssues = issueRepository.countCriticalByTeamId(teamId, startDate, endDate);
        return totalIssues > 0 ? (double) criticalIssues / totalIssues : 0.0;
    }
    
    private double calculateTeamResolutionRate(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        long totalIssues = countTeamIssues(teamId, startDate, endDate);
        long resolvedIssues = countTeamResolvedIssues(teamId, startDate, endDate);
        return totalIssues > 0 ? (double) resolvedIssues / totalIssues : 0.0;
    }
    
    private List<MemberPerformance> calculateMemberRankings(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        List<User> teamMembers = userRepository.findByTeamId(teamId);
        List<MemberPerformance> performances = new ArrayList<>();
        
        for (User member : teamMembers) {
            double completionRate = calculateReviewCompletionRate(member.getId(), startDate, endDate);
            long issuesFound = issueRepository.countIssuesFoundByReviewer(member.getId(), startDate, endDate);
            double averageScore = calculateAverageReviewScore(member.getId(), startDate, endDate);
            double fixTimeliness = calculateFixTimeliness(member.getId(), startDate, endDate);
            
            // 计算综合评分
            double overallScore = (completionRate * 0.3) + (averageScore / 10.0 * 0.3) + 
                                (fixTimeliness * 0.2) + (Math.min(issuesFound / 10.0, 1.0) * 0.2);
            
            performances.add(MemberPerformance.builder()
                    .userId(member.getId())
                    .username(member.getUsername())
                    .realName(member.getRealName())
                    .completionRate(completionRate)
                    .issuesFound(issuesFound)
                    .averageScore(averageScore)
                    .fixTimeliness(fixTimeliness)
                    .overallScore(overallScore)
                    .build());
        }
        
        // 按综合评分排序并设置排名
        performances.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        for (int i = 0; i < performances.size(); i++) {
            performances.get(i).setRank(i + 1);
        }
        
        return performances;
    }
    
    private List<FrequentIssueStats> getFrequentIssues(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = issueRepository.getFrequentIssues(teamId, startDate, endDate, 10);
        return results.stream()
                .map(map -> FrequentIssueStats.builder()
                        .issueDescription((String) map.get("description"))
                        .issueType((String) map.get("issue_type"))
                        .occurrenceCount(((Number) map.get("count")).longValue())
                        .percentage(((Number) map.get("percentage")).doubleValue())
                        .averageSeverity((String) map.get("avg_severity"))
                        .resolutionRate(((Number) map.get("resolution_rate")).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<MonthlyComparison> calculateMonthlyComparisons(Long teamId, LocalDate startDate, LocalDate endDate) {
        List<MonthlyComparison> comparisons = new ArrayList<>();
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);
        
        YearMonth current = startMonth.plusMonths(1); // 从第二个月开始，因为需要对比
        while (!current.isAfter(endMonth)) {
            MonthlyStatistics currentMonth = calculateTeamMonthlyStats(teamId, current);
            MonthlyStatistics previousMonth = calculateTeamMonthlyStats(teamId, current.minusMonths(1));
            
            double reviewCountChangeRate = calculateChangeRate(previousMonth.getReviewCount(), currentMonth.getReviewCount());
            double issueCountChangeRate = calculateChangeRate(previousMonth.getIssueCount(), currentMonth.getIssueCount());
            double scoreChangeRate = calculateChangeRate(previousMonth.getAverageScore(), currentMonth.getAverageScore());
            double completionRateChange = currentMonth.getCompletionRate() - previousMonth.getCompletionRate();
            
            comparisons.add(MonthlyComparison.builder()
                    .yearMonth(current)
                    .currentMonth(currentMonth)
                    .previousMonth(previousMonth)
                    .reviewCountChangeRate(reviewCountChangeRate)
                    .issueCountChangeRate(issueCountChangeRate)
                    .scoreChangeRate(scoreChangeRate)
                    .completionRateChange(completionRateChange)
                    .build());
            
            current = current.plusMonths(1);
        }
        
        return comparisons;
    }
    
    private MonthlyStatistics calculateTeamMonthlyStats(Long teamId, YearMonth yearMonth) {
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();
        LocalDateTime startTime = monthStart.atStartOfDay();
        LocalDateTime endTime = monthEnd.atTime(23, 59, 59);
        
        long reviewCount = reviewRecordRepository.countByTeamId(teamId, startTime, endTime);
        long issueCount = countTeamIssues(teamId, startTime, endTime);
        double averageScore = calculateTeamAverageScore(teamId, startTime, endTime);
        double completionRate = calculateTeamCoverageRate(teamId, startTime, endTime);
        double fixTimeliness = calculateTeamFixTimeliness(teamId, startTime, endTime);
        
        return MonthlyStatistics.builder()
                .yearMonth(yearMonth)
                .reviewCount(reviewCount)
                .issueCount(issueCount)
                .averageScore(averageScore)
                .completionRate(completionRate)
                .fixTimeliness(fixTimeliness)
                .build();
    }
    
    private double calculateTeamFixTimeliness(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        return fixRecordRepository.calculateTeamFixTimeliness(teamId, startDate, endDate);
    }
    
    private double calculateChangeRate(Number previous, Number current) {
        if (previous == null || current == null || previous.doubleValue() == 0) {
            return 0.0;
        }
        return (current.doubleValue() - previous.doubleValue()) / previous.doubleValue();
    }
    
    // 全局统计相关方法
    
    private int countTotalTeams() {
        return teamRepository.countAll();
    }
    
    private int countTotalUsers() {
        return userRepository.countAll();
    }
    
    private int countActiveUsers(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.countActive(startDate, endDate);
    }
    
    private long countTotalReviews(LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRecordRepository.countAll(startDate, endDate);
    }
    
    private long countTotalIssues(LocalDateTime startDate, LocalDateTime endDate) {
        return issueRepository.countAll(startDate, endDate);
    }
    
    private double calculateGlobalResolutionRate(LocalDateTime startDate, LocalDateTime endDate) {
        long totalIssues = countTotalIssues(startDate, endDate);
        long resolvedIssues = issueRepository.countAllResolved(startDate, endDate);
        return totalIssues > 0 ? (double) resolvedIssues / totalIssues : 0.0;
    }
    
    private double calculateGlobalAverageScore(LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRecordRepository.calculateGlobalAverageScore(startDate, endDate);
    }
    
    private List<TeamPerformance> calculateTeamRankings(LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = teamRepository.getTeamPerformanceData(startDate, endDate);
        List<TeamPerformance> performances = results.stream()
                .map(map -> TeamPerformance.builder()
                        .teamId(((Number) map.get("team_id")).longValue())
                        .teamName((String) map.get("team_name"))
                        .coverageRate(((Number) map.get("coverage_rate")).doubleValue())
                        .averageScore(((Number) map.get("average_score")).doubleValue())
                        .resolutionRate(((Number) map.get("resolution_rate")).doubleValue())
                        .memberCount(((Number) map.get("member_count")).intValue())
                        .overallScore(((Number) map.get("overall_score")).doubleValue())
                        .improvementTrend((String) map.get("improvement_trend"))
                        .build())
                .collect(Collectors.toList());
        
        // 按综合评分排序并设置排名
        performances.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        for (int i = 0; i < performances.size(); i++) {
            performances.get(i).setRank(i + 1);
        }
        
        return performances;
    }
    
    private Map<String, Long> getCrossTeamIssueDistribution(LocalDateTime startDate, LocalDateTime endDate) {
        List<Map<String, Object>> results = issueRepository.getCrossTeamIssueDistribution(startDate, endDate);
        return results.stream()
                .collect(Collectors.toMap(
                        map -> (String) map.get("issue_type"),
                        map -> ((Number) map.get("count")).longValue()
                ));
    }
    
    private List<UsageTrend> calculateUsageTrends(LocalDate startDate, LocalDate endDate) {
        List<UsageTrend> trends = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.atTime(23, 59, 59);
            
            int activeUsers = countActiveUsers(dayStart, dayEnd);
            long reviewCount = countTotalReviews(dayStart, dayEnd);
            long issueCount = countTotalIssues(dayStart, dayEnd);
            double usageRate = calculateDailyUsageRate(dayStart, dayEnd);
            
            trends.add(UsageTrend.builder()
                    .date(current)
                    .activeUsers(activeUsers)
                    .reviewCount(reviewCount)
                    .issueCount(issueCount)
                    .usageRate(usageRate)
                    .build());
            
            current = current.plusDays(1);
        }
        
        return trends;
    }
    
    private double calculateDailyUsageRate(LocalDateTime startDate, LocalDateTime endDate) {
        int totalUsers = countTotalUsers();
        int activeUsers = countActiveUsers(startDate, endDate);
        return totalUsers > 0 ? (double) activeUsers / totalUsers : 0.0;
    }
    
    private List<QualityImprovementTrend> calculateQualityImprovementTrends(LocalDate startDate, LocalDate endDate) {
        List<QualityImprovementTrend> trends = new ArrayList<>();
        LocalDate current = startDate;
        
        while (!current.isAfter(endDate)) {
            LocalDate weekEnd = current.plusDays(6);
            if (weekEnd.isAfter(endDate)) {
                weekEnd = endDate;
            }
            
            LocalDateTime weekStart = current.atStartOfDay();
            LocalDateTime weekEndTime = weekEnd.atTime(23, 59, 59);
            
            double globalAverageScore = calculateGlobalAverageScore(weekStart, weekEndTime);
            double resolutionRate = calculateGlobalResolutionRate(weekStart, weekEndTime);
            double criticalIssueRatio = calculateGlobalCriticalIssueRatio(weekStart, weekEndTime);
            double qualityImprovementIndex = calculateQualityImprovementIndex(globalAverageScore, resolutionRate, criticalIssueRatio);
            
            trends.add(QualityImprovementTrend.builder()
                    .date(current)
                    .globalAverageScore(globalAverageScore)
                    .resolutionRate(resolutionRate)
                    .criticalIssueRatio(criticalIssueRatio)
                    .qualityImprovementIndex(qualityImprovementIndex)
                    .build());
            
            current = current.plusWeeks(1);
        }
        
        return trends;
    }
    
    private double calculateGlobalCriticalIssueRatio(LocalDateTime startDate, LocalDateTime endDate) {
        long totalIssues = countTotalIssues(startDate, endDate);
        long criticalIssues = issueRepository.countAllCritical(startDate, endDate);
        return totalIssues > 0 ? (double) criticalIssues / totalIssues : 0.0;
    }
    
    private double calculateQualityImprovementIndex(double averageScore, double resolutionRate, double criticalIssueRatio) {
        // 质量改进指数 = (平均分数/10 * 0.4) + (解决率 * 0.4) + ((1-严重问题比例) * 0.2)
        return (averageScore / 10.0 * 0.4) + (resolutionRate * 0.4) + ((1.0 - criticalIssueRatio) * 0.2);
    }
    
    private List<BestPracticeTeam> identifyBestPracticeTeams(LocalDateTime startDate, LocalDateTime endDate) {
        List<BestPracticeTeam> bestPractices = new ArrayList<>();
        
        // 最高评审覆盖率团队
        Map<String, Object> highestCoverage = teamRepository.getTeamWithHighestCoverage(startDate, endDate);
        if (highestCoverage != null) {
            bestPractices.add(BestPracticeTeam.builder()
                    .teamId(((Number) highestCoverage.get("team_id")).longValue())
                    .teamName((String) highestCoverage.get("team_name"))
                    .category("评审覆盖率")
                    .practiceDescription("该团队在评审覆盖率方面表现优异")
                    .keyMetric("覆盖率")
                    .metricValue(((Number) highestCoverage.get("coverage_rate")).doubleValue())
                    .recommendationReason("持续保持高质量的代码评审覆盖")
                    .build());
        }
        
        // 最高问题解决率团队
        Map<String, Object> highestResolution = teamRepository.getTeamWithHighestResolutionRate(startDate, endDate);
        if (highestResolution != null) {
            bestPractices.add(BestPracticeTeam.builder()
                    .teamId(((Number) highestResolution.get("team_id")).longValue())
                    .teamName((String) highestResolution.get("team_name"))
                    .category("问题解决率")
                    .practiceDescription("该团队在问题解决效率方面表现突出")
                    .keyMetric("解决率")
                    .metricValue(((Number) highestResolution.get("resolution_rate")).doubleValue())
                    .recommendationReason("快速响应和解决代码质量问题")
                    .build());
        }
        
        // 最高代码质量团队
        Map<String, Object> highestQuality = teamRepository.getTeamWithHighestQuality(startDate, endDate);
        if (highestQuality != null) {
            bestPractices.add(BestPracticeTeam.builder()
                    .teamId(((Number) highestQuality.get("team_id")).longValue())
                    .teamName((String) highestQuality.get("team_name"))
                    .category("代码质量")
                    .practiceDescription("该团队在代码质量方面树立了标杆")
                    .keyMetric("平均质量分数")
                    .metricValue(((Number) highestQuality.get("average_score")).doubleValue())
                    .recommendationReason("严格的代码质量标准和持续改进")
                    .build());
        }
        
        return bestPractices;
    }
}