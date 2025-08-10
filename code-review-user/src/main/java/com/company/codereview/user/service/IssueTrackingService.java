package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.codereview.common.enums.IssueType;
import com.company.codereview.common.enums.Severity;
import com.company.codereview.user.entity.Issue;
import com.company.codereview.user.entity.FixRecord;
import com.company.codereview.user.entity.ReviewRecord;
import com.company.codereview.user.repository.IssueRepository;
import com.company.codereview.user.repository.FixRecordRepository;
import com.company.codereview.user.repository.ReviewRecordRepository;
import com.company.codereview.user.dto.FixRecordRequest;
import com.company.codereview.user.dto.VerificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 问题跟踪服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueTrackingService {
    
    private final IssueRepository issueRepository;
    private final FixRecordRepository fixRecordRepository;
    private final ReviewRecordRepository reviewRecordRepository;
    private final IssueNotificationService notificationService;
    
    /**
     * 获取问题跟踪仪表板数据
     */
    public IssueTrackingDashboard getTrackingDashboard(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("获取问题跟踪仪表板数据: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        // 获取团队所有问题
        List<Issue> issues = issueRepository.findByTeamId(teamId, startDate, endDate);
        
        // 统计数据
        IssueTrackingDashboard dashboard = new IssueTrackingDashboard();
        dashboard.setTotalIssues(issues.size());
        dashboard.setOpenIssues((int) issues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.OPEN).count());
        dashboard.setInProgressIssues((int) issues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.IN_PROGRESS).count());
        dashboard.setResolvedIssues((int) issues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.RESOLVED).count());
        dashboard.setClosedIssues((int) issues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.CLOSED).count());
        
        // 按严重级别统计
        Map<Severity, Long> severityStats = issues.stream()
                .collect(Collectors.groupingBy(Issue::getSeverity, Collectors.counting()));
        dashboard.setCriticalIssues(severityStats.getOrDefault(Severity.CRITICAL, 0L).intValue());
        dashboard.setMajorIssues(severityStats.getOrDefault(Severity.MAJOR, 0L).intValue());
        dashboard.setMinorIssues(severityStats.getOrDefault(Severity.MINOR, 0L).intValue());
        dashboard.setSuggestionIssues(severityStats.getOrDefault(Severity.SUGGESTION, 0L).intValue());
        
        // 按类型统计
        Map<IssueType, Long> typeStats = issues.stream()
                .collect(Collectors.groupingBy(Issue::getIssueType, Collectors.counting()));
        dashboard.setIssueTypeStats(typeStats);
        
        // 计算解决率
        int resolvedAndClosed = dashboard.getResolvedIssues() + dashboard.getClosedIssues();
        dashboard.setResolutionRate(issues.size() > 0 ? (double) resolvedAndClosed / issues.size() * 100 : 0.0);
        
        // 获取趋势数据
        dashboard.setTrendData(getIssueTrendData(teamId, startDate, endDate));
        
        // 获取热点问题
        dashboard.setHotIssues(getHotIssues(teamId, 10));
        
        return dashboard;
    }
    
    /**
     * 获取问题趋势数据
     */
    public List<IssueTrendData> getIssueTrendData(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        List<IssueTrendData> trendData = new ArrayList<>();
        
        // 按天统计问题数量
        LocalDateTime current = startDate;
        while (!current.isAfter(endDate)) {
            LocalDateTime dayStart = current.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = current.withHour(23).withMinute(59).withSecond(59);
            
            List<Issue> dayIssues = issueRepository.findByTeamId(teamId, dayStart, dayEnd);
            
            IssueTrendData data = new IssueTrendData();
            data.setDate(current.toLocalDate());
            data.setTotalIssues(dayIssues.size());
            data.setNewIssues((int) dayIssues.stream().filter(i -> i.getCreatedAt().isAfter(dayStart) && i.getCreatedAt().isBefore(dayEnd)).count());
            data.setResolvedIssues((int) dayIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.RESOLVED).count());
            
            trendData.add(data);
            current = current.plusDays(1);
        }
        
        return trendData;
    }
    
    /**
     * 获取热点问题
     */
    public List<Issue> getHotIssues(Long teamId, int limit) {
        // 获取最近30天的问题，按严重级别和创建时间排序
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Issue> issues = issueRepository.findByTeamId(teamId, thirtyDaysAgo, LocalDateTime.now());
        
        return issues.stream()
                .filter(issue -> issue.getStatus() == Issue.IssueStatus.OPEN || issue.getStatus() == Issue.IssueStatus.IN_PROGRESS)
                .sorted((a, b) -> {
                    // 先按严重级别排序
                    int severityCompare = getSeverityWeight(a.getSeverity()) - getSeverityWeight(b.getSeverity());
                    if (severityCompare != 0) {
                        return severityCompare;
                    }
                    // 再按创建时间排序（越早越优先）
                    return a.getCreatedAt().compareTo(b.getCreatedAt());
                })
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取严重级别权重
     */
    private int getSeverityWeight(Severity severity) {
        switch (severity) {
            case CRITICAL: return 1;
            case MAJOR: return 2;
            case MINOR: return 3;
            case SUGGESTION: return 4;
            default: return 5;
        }
    }
    
    /**
     * 获取用户问题分配情况
     */
    public List<UserIssueAssignment> getUserIssueAssignments(Long teamId) {
        List<Issue> issues = issueRepository.findByTeamId(teamId, null, null);
        
        // 按创建者分组统计
        Map<Long, List<Issue>> userIssuesMap = issues.stream()
                .collect(Collectors.groupingBy(Issue::getCreatedBy));
        
        List<UserIssueAssignment> assignments = new ArrayList<>();
        for (Map.Entry<Long, List<Issue>> entry : userIssuesMap.entrySet()) {
            Long userId = entry.getKey();
            List<Issue> userIssues = entry.getValue();
            
            UserIssueAssignment assignment = new UserIssueAssignment();
            assignment.setUserId(userId);
            assignment.setTotalIssues(userIssues.size());
            assignment.setOpenIssues((int) userIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.OPEN).count());
            assignment.setInProgressIssues((int) userIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.IN_PROGRESS).count());
            assignment.setResolvedIssues((int) userIssues.stream().filter(i -> i.getStatus() == Issue.IssueStatus.RESOLVED).count());
            assignment.setCriticalIssues((int) userIssues.stream().filter(i -> i.getSeverity() == Severity.CRITICAL).count());
            
            assignments.add(assignment);
        }
        
        return assignments;
    }
    
    /**
     * 自动分配问题给团队成员
     */
    @Transactional
    public void autoAssignIssues(Long teamId) {
        log.info("自动分配问题: teamId={}", teamId);
        
        // 获取未分配的问题
        QueryWrapper<Issue> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("assigned_to")
                .eq("status", Issue.IssueStatus.OPEN);
        
        List<Issue> unassignedIssues = issueRepository.selectList(queryWrapper);
        
        if (unassignedIssues.isEmpty()) {
            log.info("没有未分配的问题");
            return;
        }
        
        // 获取团队成员的工作负载
        List<UserIssueAssignment> assignments = getUserIssueAssignments(teamId);
        
        // 按工作负载排序，优先分配给负载较轻的成员
        assignments.sort(Comparator.comparingInt(a -> a.getOpenIssues() + a.getInProgressIssues()));
        
        // 轮询分配
        int assignmentIndex = 0;
        for (Issue issue : unassignedIssues) {
            if (!assignments.isEmpty()) {
                UserIssueAssignment assignment = assignments.get(assignmentIndex % assignments.size());
                
                // 更新问题分配
                issue.setAssignedTo(assignment.getUserId());
                issue.setStatus(Issue.IssueStatus.IN_PROGRESS);
                issueRepository.updateById(issue);
                
                // 更新分配统计
                assignment.setInProgressIssues(assignment.getInProgressIssues() + 1);
                
                assignmentIndex++;
                
                log.info("问题分配成功: issueId={}, userId={}", issue.getId(), assignment.getUserId());
            }
        }
    }
    
    /**
     * 升级超时问题
     */
    @Transactional
    public void escalateOverdueIssues() {
        log.info("升级超时问题");
        
        LocalDateTime overdueThreshold = LocalDateTime.now().minusDays(7); // 7天未处理视为超时
        
        QueryWrapper<Issue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", Issue.IssueStatus.OPEN)
                .lt("created_at", overdueThreshold);
        
        List<Issue> overdueIssues = issueRepository.selectList(queryWrapper);
        
        for (Issue issue : overdueIssues) {
            // 提升严重级别
            if (issue.getSeverity() == Severity.MINOR) {
                issue.setSeverity(Severity.MAJOR);
            } else if (issue.getSeverity() == Severity.MAJOR) {
                issue.setSeverity(Severity.CRITICAL);
            }
            
            // 添加超时标记
            issue.setDescription(issue.getDescription() + "\n\n[系统提醒] 此问题已超时，已自动升级严重级别。");
            
            issueRepository.updateById(issue);
            
            log.info("问题升级成功: issueId={}, newSeverity={}", issue.getId(), issue.getSeverity());
        }
    }
    
    /**
     * 获取问题解决时间统计
     */
    public IssueResolutionStats getResolutionStats(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Issue> resolvedIssues = issueRepository.findByTeamId(teamId, startDate, endDate)
                .stream()
                .filter(issue -> issue.getStatus() == Issue.IssueStatus.RESOLVED || issue.getStatus() == Issue.IssueStatus.CLOSED)
                .collect(Collectors.toList());
        
        if (resolvedIssues.isEmpty()) {
            return new IssueResolutionStats();
        }
        
        // 计算解决时间
        List<Long> resolutionTimes = resolvedIssues.stream()
                .map(this::calculateResolutionTime)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        IssueResolutionStats stats = new IssueResolutionStats();
        stats.setTotalResolvedIssues(resolvedIssues.size());
        
        if (!resolutionTimes.isEmpty()) {
            stats.setAverageResolutionTime(resolutionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0));
            stats.setMinResolutionTime(Collections.min(resolutionTimes));
            stats.setMaxResolutionTime(Collections.max(resolutionTimes));
            
            // 计算中位数
            resolutionTimes.sort(Long::compareTo);
            int size = resolutionTimes.size();
            if (size % 2 == 0) {
                stats.setMedianResolutionTime((resolutionTimes.get(size / 2 - 1) + resolutionTimes.get(size / 2)) / 2.0);
            } else {
                stats.setMedianResolutionTime(resolutionTimes.get(size / 2).doubleValue());
            }
        }
        
        return stats;
    }
    
    /**
     * 计算问题解决时间（小时）
     */
    private Long calculateResolutionTime(Issue issue) {
        if (issue.getCreatedAt() == null || issue.getUpdatedAt() == null) {
            return null;
        }
        
        return java.time.Duration.between(issue.getCreatedAt(), issue.getUpdatedAt()).toHours();
    }
    
    /**
     * 提交整改记录
     */
    @Transactional
    public FixRecord submitFixRecord(Long issueId, FixRecordRequest request) {
        log.info("提交整改记录: issueId={}, fixerId={}", issueId, request.getFixerId());
        
        Issue issue = issueRepository.selectById(issueId);
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        // 检查问题状态
        if (issue.getStatus() == Issue.IssueStatus.CLOSED || 
            issue.getStatus() == Issue.IssueStatus.REJECTED) {
            throw new RuntimeException("已关闭或已拒绝的问题不能提交整改记录");
        }
        
        FixRecord fixRecord = new FixRecord();
        fixRecord.setIssueId(issueId);
        fixRecord.setFixerId(request.getFixerId());
        fixRecord.setFixDescription(request.getFixDescription());
        fixRecord.setBeforeCodeUrl(request.getBeforeCodeUrl());
        fixRecord.setAfterCodeUrl(request.getAfterCodeUrl());
        fixRecord.setStatus(FixRecord.FixStatus.SUBMITTED);
        
        fixRecordRepository.insert(fixRecord);
        
        // 更新问题状态
        if (issue.getStatus() == Issue.IssueStatus.OPEN) {
            issue.setStatus(Issue.IssueStatus.IN_PROGRESS);
            issueRepository.updateById(issue);
        }
        
        // 发送通知
        notificationService.sendFixRecordSubmittedNotification(fixRecord, issue);
        
        log.info("整改记录提交成功: id={}", fixRecord.getId());
        return fixRecord;
    }
    
    /**
     * 验证整改记录
     */
    @Transactional
    public FixRecord verifyFixRecord(Long fixRecordId, VerificationRequest request) {
        log.info("验证整改记录: fixRecordId={}, result={}", fixRecordId, request.getResult());
        
        FixRecord fixRecord = fixRecordRepository.selectById(fixRecordId);
        if (fixRecord == null) {
            throw new RuntimeException("整改记录不存在");
        }
        
        if (fixRecord.getStatus() != FixRecord.FixStatus.SUBMITTED && 
            fixRecord.getStatus() != FixRecord.FixStatus.UNDER_REVIEW) {
            throw new RuntimeException("只有已提交或审核中的整改记录才能进行验证");
        }
        
        // 更新验证结果
        fixRecord.setVerifierId(request.getVerifierId());
        fixRecord.setVerificationResult(request.getResult());
        fixRecord.setVerificationRemarks(request.getRemarks());
        fixRecord.setVerifiedAt(LocalDateTime.now());
        
        // 根据验证结果更新状态
        Issue issue = issueRepository.selectById(fixRecord.getIssueId());
        switch (request.getResult()) {
            case PASS:
                fixRecord.setStatus(FixRecord.FixStatus.APPROVED);
                if (issue != null) {
                    issue.setStatus(Issue.IssueStatus.RESOLVED);
                    issueRepository.updateById(issue);
                }
                break;
            case FAIL:
                fixRecord.setStatus(FixRecord.FixStatus.REJECTED);
                break;
            case NEED_FURTHER_FIX:
                fixRecord.setStatus(FixRecord.FixStatus.NEED_REVISION);
                break;
        }
        
        fixRecordRepository.updateById(fixRecord);
        
        // 发送通知
        if (issue != null) {
            notificationService.sendFixRecordVerifiedNotification(fixRecord, issue);
        }
        
        log.info("整改记录验证完成: id={}, result={}", fixRecordId, request.getResult());
        return fixRecord;
    }
    
    /**
     * 获取整改跟踪详情
     */
    public IssueFixTrackingDetail getFixTrackingDetail(Long issueId) {
        log.info("获取整改跟踪详情: issueId={}", issueId);
        
        Issue issue = issueRepository.selectById(issueId);
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        List<FixRecord> fixRecords = fixRecordRepository.findByIssueId(issueId);
        
        IssueFixTrackingDetail detail = new IssueFixTrackingDetail();
        detail.setIssue(issue);
        detail.setFixRecords(fixRecords);
        detail.setTotalFixAttempts(fixRecords.size());
        detail.setCurrentStatus(issue.getStatus());
        
        // 计算整改进度
        if (!fixRecords.isEmpty()) {
            FixRecord latestRecord = fixRecords.stream()
                    .max(Comparator.comparing(FixRecord::getCreatedAt))
                    .orElse(null);
            
            if (latestRecord != null) {
                detail.setLatestFixRecord(latestRecord);
                detail.setFixProgress(calculateFixProgress(issue.getStatus(), latestRecord.getStatus()));
            }
        }
        
        // 计算整改时间统计
        detail.setFixTimeStats(calculateFixTimeStats(fixRecords));
        
        return detail;
    }
    
    /**
     * 计算整改进度
     */
    private double calculateFixProgress(Issue.IssueStatus issueStatus, FixRecord.FixStatus fixStatus) {
        if (issueStatus == Issue.IssueStatus.RESOLVED || issueStatus == Issue.IssueStatus.CLOSED) {
            return 100.0;
        }
        
        switch (fixStatus) {
            case SUBMITTED:
                return 25.0;
            case UNDER_REVIEW:
                return 50.0;
            case APPROVED:
                return 100.0;
            case REJECTED:
                return 10.0;
            case NEED_REVISION:
                return 30.0;
            default:
                return 0.0;
        }
    }
    
    /**
     * 计算整改时间统计
     */
    private FixTimeStats calculateFixTimeStats(List<FixRecord> fixRecords) {
        FixTimeStats stats = new FixTimeStats();
        
        if (fixRecords.isEmpty()) {
            return stats;
        }
        
        // 计算平均整改时间
        List<Long> fixTimes = fixRecords.stream()
                .filter(record -> record.getVerifiedAt() != null)
                .map(record -> java.time.Duration.between(record.getCreatedAt(), record.getVerifiedAt()).toHours())
                .collect(Collectors.toList());
        
        if (!fixTimes.isEmpty()) {
            stats.setAverageFixTime(fixTimes.stream().mapToLong(Long::longValue).average().orElse(0.0));
            stats.setMinFixTime(Collections.min(fixTimes));
            stats.setMaxFixTime(Collections.max(fixTimes));
        }
        
        // 统计各状态的记录数量
        Map<FixRecord.FixStatus, Long> statusCounts = fixRecords.stream()
                .collect(Collectors.groupingBy(FixRecord::getStatus, Collectors.counting()));
        
        stats.setStatusCounts(statusCounts);
        
        return stats;
    }
    
    /**
     * 获取用户的整改任务列表
     */
    public List<UserFixTask> getUserFixTasks(Long userId) {
        log.info("获取用户整改任务列表: userId={}", userId);
        
        // 获取分配给用户的问题
        List<Issue> assignedIssues = issueRepository.findAssignedToUser(userId, null);
        
        List<UserFixTask> tasks = new ArrayList<>();
        
        for (Issue issue : assignedIssues) {
            if (issue.getStatus() == Issue.IssueStatus.OPEN || 
                issue.getStatus() == Issue.IssueStatus.IN_PROGRESS) {
                
                UserFixTask task = new UserFixTask();
                task.setIssue(issue);
                
                // 获取最新的整改记录
                FixRecord latestRecord = fixRecordRepository.findLatestByIssueId(issue.getId());
                task.setLatestFixRecord(latestRecord);
                
                // 计算任务优先级
                task.setPriority(calculateTaskPriority(issue));
                
                // 计算剩余时间
                task.setRemainingTime(calculateRemainingTime(issue));
                
                tasks.add(task);
            }
        }
        
        // 按优先级排序
        tasks.sort(Comparator.comparingInt(UserFixTask::getPriority));
        
        return tasks;
    }
    
    /**
     * 计算任务优先级
     */
    private int calculateTaskPriority(Issue issue) {
        int priority = 0;
        
        // 根据严重级别设置基础优先级
        switch (issue.getSeverity()) {
            case CRITICAL:
                priority += 100;
                break;
            case MAJOR:
                priority += 50;
                break;
            case MINOR:
                priority += 20;
                break;
            case SUGGESTION:
                priority += 10;
                break;
        }
        
        // 根据创建时间调整优先级（越早创建优先级越高）
        long daysOld = java.time.Duration.between(issue.getCreatedAt(), LocalDateTime.now()).toDays();
        priority += (int) Math.min(daysOld * 2, 50);
        
        return priority;
    }
    
    /**
     * 计算剩余时间
     */
    private long calculateRemainingTime(Issue issue) {
        // 根据严重级别设置期限
        int deadlineDays;
        switch (issue.getSeverity()) {
            case CRITICAL:
                deadlineDays = 1;
                break;
            case MAJOR:
                deadlineDays = 3;
                break;
            case MINOR:
                deadlineDays = 7;
                break;
            case SUGGESTION:
                deadlineDays = 14;
                break;
            default:
                deadlineDays = 7;
        }
        
        LocalDateTime deadline = issue.getCreatedAt().plusDays(deadlineDays);
        return java.time.Duration.between(LocalDateTime.now(), deadline).toHours();
    }
    
    /**
     * 获取整改效果评估
     */
    public FixEffectivenessReport getFixEffectivenessReport(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("获取整改效果评估: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        List<Issue> issues = issueRepository.findByTeamId(teamId, startDate, endDate);
        List<FixRecord> fixRecords = new ArrayList<>();
        
        for (Issue issue : issues) {
            fixRecords.addAll(fixRecordRepository.findByIssueId(issue.getId()));
        }
        
        FixEffectivenessReport report = new FixEffectivenessReport();
        
        // 计算整改成功率
        long approvedRecords = fixRecords.stream()
                .filter(record -> record.getStatus() == FixRecord.FixStatus.APPROVED)
                .count();
        
        report.setTotalFixRecords(fixRecords.size());
        report.setApprovedFixRecords((int) approvedRecords);
        report.setSuccessRate(fixRecords.size() > 0 ? (double) approvedRecords / fixRecords.size() * 100 : 0.0);
        
        // 计算平均整改时间
        List<Long> fixTimes = fixRecords.stream()
                .filter(record -> record.getVerifiedAt() != null)
                .map(record -> java.time.Duration.between(record.getCreatedAt(), record.getVerifiedAt()).toHours())
                .collect(Collectors.toList());
        
        if (!fixTimes.isEmpty()) {
            report.setAverageFixTime(fixTimes.stream().mapToLong(Long::longValue).average().orElse(0.0));
        }
        
        // 统计各类型问题的整改情况
        Map<IssueType, FixTypeStats> typeStats = new HashMap<>();
        for (Issue issue : issues) {
            List<FixRecord> issueFixRecords = fixRecords.stream()
                    .filter(record -> record.getIssueId().equals(issue.getId()))
                    .collect(Collectors.toList());
            
            FixTypeStats stats = typeStats.computeIfAbsent(issue.getIssueType(), k -> new FixTypeStats());
            stats.setTotalIssues(stats.getTotalIssues() + 1);
            
            if (issue.getStatus() == Issue.IssueStatus.RESOLVED || issue.getStatus() == Issue.IssueStatus.CLOSED) {
                stats.setResolvedIssues(stats.getResolvedIssues() + 1);
            }
            
            stats.setTotalFixAttempts(stats.getTotalFixAttempts() + issueFixRecords.size());
        }
        
        report.setTypeStats(typeStats);
        
        return report;
    }
    
    // 内部类定义
    public static class IssueTrackingDashboard {
        private int totalIssues;
        private int openIssues;
        private int inProgressIssues;
        private int resolvedIssues;
        private int closedIssues;
        private int criticalIssues;
        private int majorIssues;
        private int minorIssues;
        private int suggestionIssues;
        private double resolutionRate;
        private Map<IssueType, Long> issueTypeStats;
        private List<IssueTrendData> trendData;
        private List<Issue> hotIssues;
        
        // Getters and Setters
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
        
        public int getCriticalIssues() { return criticalIssues; }
        public void setCriticalIssues(int criticalIssues) { this.criticalIssues = criticalIssues; }
        
        public int getMajorIssues() { return majorIssues; }
        public void setMajorIssues(int majorIssues) { this.majorIssues = majorIssues; }
        
        public int getMinorIssues() { return minorIssues; }
        public void setMinorIssues(int minorIssues) { this.minorIssues = minorIssues; }
        
        public int getSuggestionIssues() { return suggestionIssues; }
        public void setSuggestionIssues(int suggestionIssues) { this.suggestionIssues = suggestionIssues; }
        
        public double getResolutionRate() { return resolutionRate; }
        public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
        
        public Map<IssueType, Long> getIssueTypeStats() { return issueTypeStats; }
        public void setIssueTypeStats(Map<IssueType, Long> issueTypeStats) { this.issueTypeStats = issueTypeStats; }
        
        public List<IssueTrendData> getTrendData() { return trendData; }
        public void setTrendData(List<IssueTrendData> trendData) { this.trendData = trendData; }
        
        public List<Issue> getHotIssues() { return hotIssues; }
        public void setHotIssues(List<Issue> hotIssues) { this.hotIssues = hotIssues; }
    }
    
    public static class IssueTrendData {
        private java.time.LocalDate date;
        private int totalIssues;
        private int newIssues;
        private int resolvedIssues;
        
        // Getters and Setters
        public java.time.LocalDate getDate() { return date; }
        public void setDate(java.time.LocalDate date) { this.date = date; }
        
        public int getTotalIssues() { return totalIssues; }
        public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }
        
        public int getNewIssues() { return newIssues; }
        public void setNewIssues(int newIssues) { this.newIssues = newIssues; }
        
        public int getResolvedIssues() { return resolvedIssues; }
        public void setResolvedIssues(int resolvedIssues) { this.resolvedIssues = resolvedIssues; }
    }
    
    public static class UserIssueAssignment {
        private Long userId;
        private int totalIssues;
        private int openIssues;
        private int inProgressIssues;
        private int resolvedIssues;
        private int criticalIssues;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public int getTotalIssues() { return totalIssues; }
        public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }
        
        public int getOpenIssues() { return openIssues; }
        public void setOpenIssues(int openIssues) { this.openIssues = openIssues; }
        
        public int getInProgressIssues() { return inProgressIssues; }
        public void setInProgressIssues(int inProgressIssues) { this.inProgressIssues = inProgressIssues; }
        
        public int getResolvedIssues() { return resolvedIssues; }
        public void setResolvedIssues(int resolvedIssues) { this.resolvedIssues = resolvedIssues; }
        
        public int getCriticalIssues() { return criticalIssues; }
        public void setCriticalIssues(int criticalIssues) { this.criticalIssues = criticalIssues; }
    }
    
    public static class IssueResolutionStats {
        private int totalResolvedIssues;
        private double averageResolutionTime;
        private double medianResolutionTime;
        private long minResolutionTime;
        private long maxResolutionTime;
        
        // Getters and Setters
        public int getTotalResolvedIssues() { return totalResolvedIssues; }
        public void setTotalResolvedIssues(int totalResolvedIssues) { this.totalResolvedIssues = totalResolvedIssues; }
        
        public double getAverageResolutionTime() { return averageResolutionTime; }
        public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
        
        public double getMedianResolutionTime() { return medianResolutionTime; }
        public void setMedianResolutionTime(double medianResolutionTime) { this.medianResolutionTime = medianResolutionTime; }
        
        public long getMinResolutionTime() { return minResolutionTime; }
        public void setMinResolutionTime(long minResolutionTime) { this.minResolutionTime = minResolutionTime; }
        
        public long getMaxResolutionTime() { return maxResolutionTime; }
        public void setMaxResolutionTime(long maxResolutionTime) { this.maxResolutionTime = maxResolutionTime; }
    }
    
    // 新增的内部类定义
    public static class IssueFixTrackingDetail {
        private Issue issue;
        private List<FixRecord> fixRecords;
        private FixRecord latestFixRecord;
        private int totalFixAttempts;
        private Issue.IssueStatus currentStatus;
        private double fixProgress;
        private FixTimeStats fixTimeStats;
        
        // Getters and Setters
        public Issue getIssue() { return issue; }
        public void setIssue(Issue issue) { this.issue = issue; }
        
        public List<FixRecord> getFixRecords() { return fixRecords; }
        public void setFixRecords(List<FixRecord> fixRecords) { this.fixRecords = fixRecords; }
        
        public FixRecord getLatestFixRecord() { return latestFixRecord; }
        public void setLatestFixRecord(FixRecord latestFixRecord) { this.latestFixRecord = latestFixRecord; }
        
        public int getTotalFixAttempts() { return totalFixAttempts; }
        public void setTotalFixAttempts(int totalFixAttempts) { this.totalFixAttempts = totalFixAttempts; }
        
        public Issue.IssueStatus getCurrentStatus() { return currentStatus; }
        public void setCurrentStatus(Issue.IssueStatus currentStatus) { this.currentStatus = currentStatus; }
        
        public double getFixProgress() { return fixProgress; }
        public void setFixProgress(double fixProgress) { this.fixProgress = fixProgress; }
        
        public FixTimeStats getFixTimeStats() { return fixTimeStats; }
        public void setFixTimeStats(FixTimeStats fixTimeStats) { this.fixTimeStats = fixTimeStats; }
    }
    
    public static class FixTimeStats {
        private double averageFixTime;
        private long minFixTime;
        private long maxFixTime;
        private Map<FixRecord.FixStatus, Long> statusCounts;
        
        // Getters and Setters
        public double getAverageFixTime() { return averageFixTime; }
        public void setAverageFixTime(double averageFixTime) { this.averageFixTime = averageFixTime; }
        
        public long getMinFixTime() { return minFixTime; }
        public void setMinFixTime(long minFixTime) { this.minFixTime = minFixTime; }
        
        public long getMaxFixTime() { return maxFixTime; }
        public void setMaxFixTime(long maxFixTime) { this.maxFixTime = maxFixTime; }
        
        public Map<FixRecord.FixStatus, Long> getStatusCounts() { return statusCounts; }
        public void setStatusCounts(Map<FixRecord.FixStatus, Long> statusCounts) { this.statusCounts = statusCounts; }
    }
    
    public static class UserFixTask {
        private Issue issue;
        private FixRecord latestFixRecord;
        private int priority;
        private long remainingTime;
        
        // Getters and Setters
        public Issue getIssue() { return issue; }
        public void setIssue(Issue issue) { this.issue = issue; }
        
        public FixRecord getLatestFixRecord() { return latestFixRecord; }
        public void setLatestFixRecord(FixRecord latestFixRecord) { this.latestFixRecord = latestFixRecord; }
        
        public int getPriority() { return priority; }
        public void setPriority(int priority) { this.priority = priority; }
        
        public long getRemainingTime() { return remainingTime; }
        public void setRemainingTime(long remainingTime) { this.remainingTime = remainingTime; }
    }
    
    public static class FixEffectivenessReport {
        private int totalFixRecords;
        private int approvedFixRecords;
        private double successRate;
        private double averageFixTime;
        private Map<IssueType, FixTypeStats> typeStats;
        
        // Getters and Setters
        public int getTotalFixRecords() { return totalFixRecords; }
        public void setTotalFixRecords(int totalFixRecords) { this.totalFixRecords = totalFixRecords; }
        
        public int getApprovedFixRecords() { return approvedFixRecords; }
        public void setApprovedFixRecords(int approvedFixRecords) { this.approvedFixRecords = approvedFixRecords; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getAverageFixTime() { return averageFixTime; }
        public void setAverageFixTime(double averageFixTime) { this.averageFixTime = averageFixTime; }
        
        public Map<IssueType, FixTypeStats> getTypeStats() { return typeStats; }
        public void setTypeStats(Map<IssueType, FixTypeStats> typeStats) { this.typeStats = typeStats; }
    }
    
    public static class FixTypeStats {
        private int totalIssues;
        private int resolvedIssues;
        private int totalFixAttempts;
        
        // Getters and Setters
        public int getTotalIssues() { return totalIssues; }
        public void setTotalIssues(int totalIssues) { this.totalIssues = totalIssues; }
        
        public int getResolvedIssues() { return resolvedIssues; }
        public void setResolvedIssues(int resolvedIssues) { this.resolvedIssues = resolvedIssues; }
        
        public int getTotalFixAttempts() { return totalFixAttempts; }
        public void setTotalFixAttempts(int totalFixAttempts) { this.totalFixAttempts = totalFixAttempts; }
    }
}