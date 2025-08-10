package com.company.codereview.user.service;

import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.entity.Issue;
import com.company.codereview.user.entity.FixRecord;
import com.company.codereview.user.repository.IssueRepository;
import com.company.codereview.user.repository.FixRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 整改跟踪度量服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FixTrackingMetricsService {
    
    private final IssueRepository issueRepository;
    private final FixRecordRepository fixRecordRepository;
    
    /**
     * 计算整改效率指标
     */
    public FixEfficiencyMetrics calculateFixEfficiencyMetrics(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("计算整改效率指标: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        List<Issue> issues = issueRepository.findByTeamId(teamId, startDate, endDate);
        List<FixRecord> allFixRecords = new ArrayList<>();
        
        for (Issue issue : issues) {
            allFixRecords.addAll(fixRecordRepository.findByIssueId(issue.getId()));
        }
        
        FixEfficiencyMetrics metrics = new FixEfficiencyMetrics();
        
        // 计算平均首次整改时间
        metrics.setAverageFirstFixTime(calculateAverageFirstFixTime(issues));
        
        // 计算整改成功率
        metrics.setFixSuccessRate(calculateFixSuccessRate(allFixRecords));
        
        // 计算平均整改轮次
        metrics.setAverageFixRounds(calculateAverageFixRounds(issues));
        
        // 计算整改及时率
        metrics.setFixTimelinessRate(calculateFixTimelinessRate(issues));
        
        // 计算重复整改率
        metrics.setRepeatFixRate(calculateRepeatFixRate(issues));
        
        return metrics;
    }
    
    /**
     * 计算平均首次整改时间
     */
    private double calculateAverageFirstFixTime(List<Issue> issues) {
        List<Long> firstFixTimes = new ArrayList<>();
        
        for (Issue issue : issues) {
            List<FixRecord> fixRecords = fixRecordRepository.findByIssueId(issue.getId());
            if (!fixRecords.isEmpty()) {
                FixRecord firstRecord = fixRecords.stream()
                        .min(Comparator.comparing(FixRecord::getCreatedAt))
                        .orElse(null);
                
                if (firstRecord != null) {
                    long hours = java.time.Duration.between(issue.getCreatedAt(), firstRecord.getCreatedAt()).toHours();
                    firstFixTimes.add(hours);
                }
            }
        }
        
        return firstFixTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }
    
    /**
     * 计算整改成功率
     */
    private double calculateFixSuccessRate(List<FixRecord> fixRecords) {
        if (fixRecords.isEmpty()) {
            return 0.0;
        }
        
        long successfulFixes = fixRecords.stream()
                .filter(record -> record.getStatus() == FixRecord.FixStatus.APPROVED)
                .count();
        
        return (double) successfulFixes / fixRecords.size() * 100;
    }
    
    /**
     * 计算平均整改轮次
     */
    private double calculateAverageFixRounds(List<Issue> issues) {
        List<Integer> fixRounds = new ArrayList<>();
        
        for (Issue issue : issues) {
            List<FixRecord> fixRecords = fixRecordRepository.findByIssueId(issue.getId());
            if (!fixRecords.isEmpty()) {
                fixRounds.add(fixRecords.size());
            }
        }
        
        return fixRounds.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
    
    /**
     * 计算整改及时率
     */
    private double calculateFixTimelinessRate(List<Issue> issues) {
        int timelyFixes = 0;
        int totalFixes = 0;
        
        for (Issue issue : issues) {
            if (issue.getStatus() == Issue.IssueStatus.RESOLVED || 
                issue.getStatus() == Issue.IssueStatus.CLOSED) {
                
                totalFixes++;
                
                // 根据严重级别确定期限
                int deadlineDays = getDeadlineDays(issue.getSeverity());
                LocalDateTime deadline = issue.getCreatedAt().plusDays(deadlineDays);
                
                if (issue.getUpdatedAt() != null && issue.getUpdatedAt().isBefore(deadline)) {
                    timelyFixes++;
                }
            }
        }
        
        return totalFixes > 0 ? (double) timelyFixes / totalFixes * 100 : 0.0;
    }
    
    /**
     * 计算重复整改率
     */
    private double calculateRepeatFixRate(List<Issue> issues) {
        int repeatFixIssues = 0;
        int totalIssues = issues.size();
        
        for (Issue issue : issues) {
            List<FixRecord> fixRecords = fixRecordRepository.findByIssueId(issue.getId());
            if (fixRecords.size() > 1) {
                repeatFixIssues++;
            }
        }
        
        return totalIssues > 0 ? (double) repeatFixIssues / totalIssues * 100 : 0.0;
    }
    
    /**
     * 根据严重级别获取期限天数
     */
    private int getDeadlineDays(Severity severity) {
        switch (severity) {
            case CRITICAL:
                return 1;
            case MAJOR:
                return 3;
            case MINOR:
                return 7;
            case SUGGESTION:
                return 14;
            default:
                return 7;
        }
    }
    
    /**
     * 计算质量改进指标
     */
    public QualityImprovementMetrics calculateQualityImprovementMetrics(Long teamId, 
                                                                       LocalDateTime currentPeriodStart, 
                                                                       LocalDateTime currentPeriodEnd,
                                                                       LocalDateTime previousPeriodStart,
                                                                       LocalDateTime previousPeriodEnd) {
        log.info("计算质量改进指标: teamId={}", teamId);
        
        // 获取当前周期和上一周期的数据
        List<Issue> currentIssues = issueRepository.findByTeamId(teamId, currentPeriodStart, currentPeriodEnd);
        List<Issue> previousIssues = issueRepository.findByTeamId(teamId, previousPeriodStart, previousPeriodEnd);
        
        QualityImprovementMetrics metrics = new QualityImprovementMetrics();
        
        // 计算问题数量变化
        metrics.setCurrentPeriodIssues(currentIssues.size());
        metrics.setPreviousPeriodIssues(previousIssues.size());
        metrics.setIssueCountChange(calculatePercentageChange(previousIssues.size(), currentIssues.size()));
        
        // 计算各严重级别问题的变化
        metrics.setCriticalIssueChange(calculateSeverityChange(previousIssues, currentIssues, Severity.CRITICAL));
        metrics.setMajorIssueChange(calculateSeverityChange(previousIssues, currentIssues, Severity.MAJOR));
        metrics.setMinorIssueChange(calculateSeverityChange(previousIssues, currentIssues, Severity.MINOR));
        
        // 计算各类型问题的变化
        Map<IssueType, Double> typeChanges = new HashMap<>();
        for (IssueType type : IssueType.values()) {
            double change = calculateTypeChange(previousIssues, currentIssues, type);
            typeChanges.put(type, change);
        }
        metrics.setIssueTypeChanges(typeChanges);
        
        // 计算解决率变化
        double currentResolutionRate = calculateResolutionRate(currentIssues);
        double previousResolutionRate = calculateResolutionRate(previousIssues);
        metrics.setResolutionRateChange(currentResolutionRate - previousResolutionRate);
        
        return metrics;
    }
    
    /**
     * 计算百分比变化
     */
    private double calculatePercentageChange(int previous, int current) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }
    
    /**
     * 计算严重级别问题变化
     */
    private double calculateSeverityChange(List<Issue> previousIssues, List<Issue> currentIssues, Severity severity) {
        int previousCount = (int) previousIssues.stream().filter(i -> i.getSeverity() == severity).count();
        int currentCount = (int) currentIssues.stream().filter(i -> i.getSeverity() == severity).count();
        
        return calculatePercentageChange(previousCount, currentCount);
    }
    
    /**
     * 计算问题类型变化
     */
    private double calculateTypeChange(List<Issue> previousIssues, List<Issue> currentIssues, IssueType type) {
        int previousCount = (int) previousIssues.stream().filter(i -> i.getIssueType() == type).count();
        int currentCount = (int) currentIssues.stream().filter(i -> i.getIssueType() == type).count();
        
        return calculatePercentageChange(previousCount, currentCount);
    }
    
    /**
     * 计算解决率
     */
    private double calculateResolutionRate(List<Issue> issues) {
        if (issues.isEmpty()) {
            return 0.0;
        }
        
        long resolvedCount = issues.stream()
                .filter(i -> i.getStatus() == Issue.IssueStatus.RESOLVED || 
                           i.getStatus() == Issue.IssueStatus.CLOSED)
                .count();
        
        return (double) resolvedCount / issues.size() * 100;
    }
    
    /**
     * 计算团队成员表现指标
     */
    public List<MemberPerformanceMetrics> calculateMemberPerformanceMetrics(Long teamId, 
                                                                           LocalDateTime startDate, 
                                                                           LocalDateTime endDate) {
        log.info("计算团队成员表现指标: teamId={}", teamId);
        
        List<Issue> issues = issueRepository.findByTeamId(teamId, startDate, endDate);
        Map<Long, List<Issue>> memberIssues = issues.stream()
                .filter(issue -> issue.getAssignedTo() != null)
                .collect(Collectors.groupingBy(Issue::getAssignedTo));
        
        List<MemberPerformanceMetrics> memberMetrics = new ArrayList<>();
        
        for (Map.Entry<Long, List<Issue>> entry : memberIssues.entrySet()) {
            Long memberId = entry.getKey();
            List<Issue> memberIssueList = entry.getValue();
            
            MemberPerformanceMetrics metrics = new MemberPerformanceMetrics();
            metrics.setMemberId(memberId);
            metrics.setTotalAssignedIssues(memberIssueList.size());
            
            // 计算解决的问题数量
            int resolvedIssues = (int) memberIssueList.stream()
                    .filter(i -> i.getStatus() == Issue.IssueStatus.RESOLVED || 
                               i.getStatus() == Issue.IssueStatus.CLOSED)
                    .count();
            metrics.setResolvedIssues(resolvedIssues);
            
            // 计算解决率
            metrics.setResolutionRate(memberIssueList.size() > 0 ? 
                    (double) resolvedIssues / memberIssueList.size() * 100 : 0.0);
            
            // 计算平均解决时间
            List<Long> resolutionTimes = memberIssueList.stream()
                    .filter(i -> i.getStatus() == Issue.IssueStatus.RESOLVED || 
                               i.getStatus() == Issue.IssueStatus.CLOSED)
                    .filter(i -> i.getUpdatedAt() != null)
                    .map(i -> java.time.Duration.between(i.getCreatedAt(), i.getUpdatedAt()).toHours())
                    .collect(Collectors.toList());
            
            if (!resolutionTimes.isEmpty()) {
                metrics.setAverageResolutionTime(resolutionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0));
            }
            
            // 计算及时完成率
            int timelyCompletions = 0;
            for (Issue issue : memberIssueList) {
                if (issue.getStatus() == Issue.IssueStatus.RESOLVED || 
                    issue.getStatus() == Issue.IssueStatus.CLOSED) {
                    
                    int deadlineDays = getDeadlineDays(issue.getSeverity());
                    LocalDateTime deadline = issue.getCreatedAt().plusDays(deadlineDays);
                    
                    if (issue.getUpdatedAt() != null && issue.getUpdatedAt().isBefore(deadline)) {
                        timelyCompletions++;
                    }
                }
            }
            
            metrics.setTimelyCompletionRate(resolvedIssues > 0 ? 
                    (double) timelyCompletions / resolvedIssues * 100 : 0.0);
            
            // 计算质量分数（基于整改成功率）
            List<FixRecord> memberFixRecords = new ArrayList<>();
            for (Issue issue : memberIssueList) {
                memberFixRecords.addAll(fixRecordRepository.findByIssueId(issue.getId()));
            }
            
            if (!memberFixRecords.isEmpty()) {
                long successfulFixes = memberFixRecords.stream()
                        .filter(record -> record.getStatus() == FixRecord.FixStatus.APPROVED)
                        .count();
                metrics.setQualityScore((double) successfulFixes / memberFixRecords.size() * 100);
            }
            
            memberMetrics.add(metrics);
        }
        
        // 按解决率排序
        memberMetrics.sort((a, b) -> Double.compare(b.getResolutionRate(), a.getResolutionRate()));
        
        return memberMetrics;
    }
    
    /**
     * 生成整改趋势报告
     */
    public FixTrendReport generateFixTrendReport(Long teamId, LocalDateTime startDate, LocalDateTime endDate, int intervalDays) {
        log.info("生成整改趋势报告: teamId={}, intervalDays={}", teamId, intervalDays);
        
        FixTrendReport report = new FixTrendReport();
        List<TrendDataPoint> trendData = new ArrayList<>();
        
        LocalDateTime current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDateTime intervalEnd = current.plusDays(intervalDays);
            if (intervalEnd.isAfter(endDate)) {
                intervalEnd = endDate;
            }
            
            List<Issue> intervalIssues = issueRepository.findByTeamId(teamId, current, intervalEnd);
            
            TrendDataPoint dataPoint = new TrendDataPoint();
            dataPoint.setDate(current.toLocalDate());
            dataPoint.setTotalIssues(intervalIssues.size());
            
            // 统计各状态的问题数量
            dataPoint.setOpenIssues((int) intervalIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.OPEN).count());
            dataPoint.setInProgressIssues((int) intervalIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.IN_PROGRESS).count());
            dataPoint.setResolvedIssues((int) intervalIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.RESOLVED).count());
            dataPoint.setClosedIssues((int) intervalIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.CLOSED).count());
            
            // 计算解决率
            int totalResolved = dataPoint.getResolvedIssues() + dataPoint.getClosedIssues();
            dataPoint.setResolutionRate(intervalIssues.size() > 0 ? (double) totalResolved / intervalIssues.size() * 100 : 0.0);
            
            trendData.add(dataPoint);
            current = intervalEnd;
        }
        
        report.setTrendData(trendData);
        report.setStartDate(startDate.toLocalDate());
        report.setEndDate(endDate.toLocalDate());
        report.setIntervalDays(intervalDays);
        
        return report;
    }
    
    // 内部类定义
    public static class FixEfficiencyMetrics {
        private double averageFirstFixTime; // 平均首次整改时间（小时）
        private double fixSuccessRate; // 整改成功率（%）
        private double averageFixRounds; // 平均整改轮次
        private double fixTimelinessRate; // 整改及时率（%）
        private double repeatFixRate; // 重复整改率（%）
        
        // Getters and Setters
        public double getAverageFirstFixTime() { return averageFirstFixTime; }
        public void setAverageFirstFixTime(double averageFirstFixTime) { this.averageFirstFixTime = averageFirstFixTime; }
        
        public double getFixSuccessRate() { return fixSuccessRate; }
        public void setFixSuccessRate(double fixSuccessRate) { this.fixSuccessRate = fixSuccessRate; }
        
        public double getAverageFixRounds() { return averageFixRounds; }
        public void setAverageFixRounds(double averageFixRounds) { this.averageFixRounds = averageFixRounds; }
        
        public double getFixTimelinessRate() { return fixTimelinessRate; }
        public void setFixTimelinessRate(double fixTimelinessRate) { this.fixTimelinessRate = fixTimelinessRate; }
        
        public double getRepeatFixRate() { return repeatFixRate; }
        public void setRepeatFixRate(double repeatFixRate) { this.repeatFixRate = repeatFixRate; }
    }
    
    public static class QualityImprovementMetrics {
        private int currentPeriodIssues;
        private int previousPeriodIssues;
        private double issueCountChange; // 问题数量变化（%）
        private double criticalIssueChange; // 严重问题变化（%）
        private double majorIssueChange; // 重要问题变化（%）
        private double minorIssueChange; // 轻微问题变化（%）
        private Map<IssueType, Double> issueTypeChanges; // 各类型问题变化
        private double resolutionRateChange; // 解决率变化
        
        // Getters and Setters
        public int getCurrentPeriodIssues() { return currentPeriodIssues; }
        public void setCurrentPeriodIssues(int currentPeriodIssues) { this.currentPeriodIssues = currentPeriodIssues; }
        
        public int getPreviousPeriodIssues() { return previousPeriodIssues; }
        public void setPreviousPeriodIssues(int previousPeriodIssues) { this.previousPeriodIssues = previousPeriodIssues; }
        
        public double getIssueCountChange() { return issueCountChange; }
        public void setIssueCountChange(double issueCountChange) { this.issueCountChange = issueCountChange; }
        
        public double getCriticalIssueChange() { return criticalIssueChange; }
        public void setCriticalIssueChange(double criticalIssueChange) { this.criticalIssueChange = criticalIssueChange; }
        
        public double getMajorIssueChange() { return majorIssueChange; }
        public void setMajorIssueChange(double majorIssueChange) { this.majorIssueChange = majorIssueChange; }
        
        public double getMinorIssueChange() { return minorIssueChange; }
        public void setMinorIssueChange(double minorIssueChange) { this.minorIssueChange = minorIssueChange; }
        
        public Map<IssueType, Double> getIssueTypeChanges() { return issueTypeChanges; }
        public void setIssueTypeChanges(Map<IssueType, Double> issueTypeChanges) { this.issueTypeChanges = issueTypeChanges; }
        
        public double getResolutionRateChange() { return resolutionRateChange; }
        public void setResolutionRateChange(double resolutionRateChange) { this.resolutionRateChange = resolutionRateChange; }
    }
    
    public static class MemberPerformanceMetrics {
        private Long memberId;
        private int totalAssignedIssues;
        private int resolvedIssues;
        private double resolutionRate;
        private double averageResolutionTime;
        private double timelyCompletionRate;
        private double qualityScore;
        
        // Getters and Setters
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        
        public int getTotalAssignedIssues() { return totalAssignedIssues; }
        public void setTotalAssignedIssues(int totalAssignedIssues) { this.totalAssignedIssues = totalAssignedIssues; }
        
        public int getResolvedIssues() { return resolvedIssues; }
        public void setResolvedIssues(int resolvedIssues) { this.resolvedIssues = resolvedIssues; }
        
        public double getResolutionRate() { return resolutionRate; }
        public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
        
        public double getAverageResolutionTime() { return averageResolutionTime; }
        public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
        
        public double getTimelyCompletionRate() { return timelyCompletionRate; }
        public void setTimelyCompletionRate(double timelyCompletionRate) { this.timelyCompletionRate = timelyCompletionRate; }
        
        public double getQualityScore() { return qualityScore; }
        public void setQualityScore(double qualityScore) { this.qualityScore = qualityScore; }
    }
    
    public static class FixTrendReport {
        private java.time.LocalDate startDate;
        private java.time.LocalDate endDate;
        private int intervalDays;
        private List<TrendDataPoint> trendData;
        
        // Getters and Setters
        public java.time.LocalDate getStartDate() { return startDate; }
        public void setStartDate(java.time.LocalDate startDate) { this.startDate = startDate; }
        
        public java.time.LocalDate getEndDate() { return endDate; }
        public void setEndDate(java.time.LocalDate endDate) { this.endDate = endDate; }
        
        public int getIntervalDays() { return intervalDays; }
        public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }
        
        public List<TrendDataPoint> getTrendData() { return trendData; }
        public void setTrendData(List<TrendDataPoint> trendData) { this.trendData = trendData; }
    }
    
    public static class TrendDataPoint {
        private java.time.LocalDate date;
        private int totalIssues;
        private int openIssues;
        private int inProgressIssues;
        private int resolvedIssues;
        private int closedIssues;
        private double resolutionRate;
        
        // Getters and Setters
        public java.time.LocalDate getDate() { return date; }
        public void setDate(java.time.LocalDate date) { this.date = date; }
        
        public int getTotalIssues() { return totalIssues; }
        public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }
        
        public int getOpenIssues() { return openIssues; }
        public void setOpenIssues(int openIssues) { this.openIssues = openIssues; }
        
        public int getInProgressIssues() { return inProgressIssues; }
        public void setInProgressIssues(int inProgressIssues) { this.inProgressIssues = inProgressIssues; }
        
        public int getResolvedIssues() { return resolvedIssues; }
        public void setResolvedIssues(int resolvedIssues) { this.resolvedIssues = resolvedIssues; }
        
        public int getClosedIssues() { return closedIssues; }
        public void setClosedIssues(int closedIssues) { this.closedIssues = closedIssues; }
        
        public double getResolutionRate() { return resolutionRate; }
        public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
    }
}