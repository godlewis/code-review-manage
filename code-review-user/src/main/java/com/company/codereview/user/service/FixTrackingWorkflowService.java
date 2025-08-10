package com.company.codereview.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.company.codereview.user.entity.Issue;
import com.company.codereview.user.entity.FixRecord;
import com.company.codereview.user.repository.IssueRepository;
import com.company.codereview.user.repository.FixRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 整改跟踪工作流服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FixTrackingWorkflowService {
    
    private final IssueRepository issueRepository;
    private final FixRecordRepository fixRecordRepository;
    private final IssueNotificationService notificationService;
    
    /**
     * 启动整改流程
     */
    @Transactional
    public FixTrackingWorkflow startFixWorkflow(Long issueId, Long assigneeId) {
        log.info("启动整改流程: issueId={}, assigneeId={}", issueId, assigneeId);
        
        Issue issue = issueRepository.selectById(issueId);
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        if (issue.getStatus() != Issue.IssueStatus.OPEN) {
            throw new RuntimeException("只有待处理的问题才能启动整改流程");
        }
        
        // 分配问题给指定用户
        issue.setAssignedTo(assigneeId);
        issue.setStatus(Issue.IssueStatus.IN_PROGRESS);
        issueRepository.updateById(issue);
        
        // 创建工作流实例
        FixTrackingWorkflow workflow = new FixTrackingWorkflow();
        workflow.setIssueId(issueId);
        workflow.setAssigneeId(assigneeId);
        workflow.setCurrentStage(WorkflowStage.ASSIGNED);
        workflow.setStartTime(LocalDateTime.now());
        workflow.setDeadline(calculateDeadline(issue));
        
        // 发送分配通知
        notificationService.sendIssueAssignedNotification(issue, null, assigneeId);
        
        log.info("整改流程启动成功: issueId={}, workflowId={}", issueId, workflow.getId());
        return workflow;
    }
    
    /**
     * 推进工作流到下一阶段
     */
    @Transactional
    public FixTrackingWorkflow advanceWorkflow(Long issueId, WorkflowAction action, Map<String, Object> parameters) {
        log.info("推进工作流: issueId={}, action={}", issueId, action);
        
        Issue issue = issueRepository.selectById(issueId);
        if (issue == null) {
            throw new RuntimeException("问题不存在");
        }
        
        FixTrackingWorkflow workflow = getCurrentWorkflow(issueId);
        if (workflow == null) {
            throw new RuntimeException("工作流不存在");
        }
        
        // 根据当前阶段和动作推进工作流
        WorkflowStage nextStage = determineNextStage(workflow.getCurrentStage(), action);
        
        // 执行阶段转换逻辑
        executeStageTransition(workflow, nextStage, action, parameters);
        
        // 更新工作流状态
        workflow.setCurrentStage(nextStage);
        workflow.setLastActionTime(LocalDateTime.now());
        
        // 检查是否完成
        if (nextStage == WorkflowStage.COMPLETED || nextStage == WorkflowStage.CANCELLED) {
            workflow.setEndTime(LocalDateTime.now());
            workflow.setCompleted(true);
        }
        
        log.info("工作流推进成功: issueId={}, newStage={}", issueId, nextStage);
        return workflow;
    }
    
    /**
     * 获取当前工作流状态
     */
    public FixTrackingWorkflow getCurrentWorkflow(Long issueId) {
        // 这里应该从数据库或缓存中获取工作流状态
        // 为了简化，这里创建一个模拟的工作流状态
        Issue issue = issueRepository.selectById(issueId);
        if (issue == null) {
            return null;
        }
        
        FixTrackingWorkflow workflow = new FixTrackingWorkflow();
        workflow.setIssueId(issueId);
        workflow.setAssigneeId(issue.getAssignedTo());
        workflow.setCurrentStage(mapIssueStatusToWorkflowStage(issue.getStatus()));
        workflow.setDeadline(calculateDeadline(issue));
        
        return workflow;
    }
    
    /**
     * 映射问题状态到工作流阶段
     */
    private WorkflowStage mapIssueStatusToWorkflowStage(Issue.IssueStatus status) {
        switch (status) {
            case OPEN:
                return WorkflowStage.CREATED;
            case IN_PROGRESS:
                return WorkflowStage.IN_PROGRESS;
            case RESOLVED:
                return WorkflowStage.COMPLETED;
            case CLOSED:
                return WorkflowStage.COMPLETED;
            case REJECTED:
                return WorkflowStage.CANCELLED;
            default:
                return WorkflowStage.CREATED;
        }
    }
    
    /**
     * 确定下一个工作流阶段
     */
    private WorkflowStage determineNextStage(WorkflowStage currentStage, WorkflowAction action) {
        switch (currentStage) {
            case CREATED:
                if (action == WorkflowAction.ASSIGN) {
                    return WorkflowStage.ASSIGNED;
                }
                break;
            case ASSIGNED:
                if (action == WorkflowAction.START_FIX) {
                    return WorkflowStage.IN_PROGRESS;
                } else if (action == WorkflowAction.REJECT) {
                    return WorkflowStage.CANCELLED;
                }
                break;
            case IN_PROGRESS:
                if (action == WorkflowAction.SUBMIT_FIX) {
                    return WorkflowStage.UNDER_REVIEW;
                } else if (action == WorkflowAction.CANCEL) {
                    return WorkflowStage.CANCELLED;
                }
                break;
            case UNDER_REVIEW:
                if (action == WorkflowAction.APPROVE) {
                    return WorkflowStage.COMPLETED;
                } else if (action == WorkflowAction.REJECT) {
                    return WorkflowStage.IN_PROGRESS;
                } else if (action == WorkflowAction.REQUEST_REVISION) {
                    return WorkflowStage.REVISION_REQUIRED;
                }
                break;
            case REVISION_REQUIRED:
                if (action == WorkflowAction.RESUBMIT) {
                    return WorkflowStage.UNDER_REVIEW;
                }
                break;
        }
        
        throw new RuntimeException("无效的工作流状态转换: " + currentStage + " -> " + action);
    }
    
    /**
     * 执行阶段转换逻辑
     */
    private void executeStageTransition(FixTrackingWorkflow workflow, WorkflowStage nextStage, 
                                      WorkflowAction action, Map<String, Object> parameters) {
        
        Issue issue = issueRepository.selectById(workflow.getIssueId());
        
        switch (nextStage) {
            case ASSIGNED:
                // 分配问题
                Long assigneeId = (Long) parameters.get("assigneeId");
                issue.setAssignedTo(assigneeId);
                issue.setStatus(Issue.IssueStatus.IN_PROGRESS);
                issueRepository.updateById(issue);
                break;
                
            case IN_PROGRESS:
                // 开始整改
                issue.setStatus(Issue.IssueStatus.IN_PROGRESS);
                issueRepository.updateById(issue);
                break;
                
            case UNDER_REVIEW:
                // 提交整改记录
                createFixRecord(workflow, parameters);
                break;
                
            case COMPLETED:
                // 完成整改
                issue.setStatus(Issue.IssueStatus.RESOLVED);
                issueRepository.updateById(issue);
                break;
                
            case CANCELLED:
                // 取消整改
                issue.setStatus(Issue.IssueStatus.REJECTED);
                issueRepository.updateById(issue);
                break;
                
            case REVISION_REQUIRED:
                // 需要修订
                updateFixRecordStatus(workflow, FixRecord.FixStatus.NEED_REVISION, parameters);
                break;
        }
    }
    
    /**
     * 创建整改记录
     */
    private void createFixRecord(FixTrackingWorkflow workflow, Map<String, Object> parameters) {
        FixRecord fixRecord = new FixRecord();
        fixRecord.setIssueId(workflow.getIssueId());
        fixRecord.setFixerId(workflow.getAssigneeId());
        fixRecord.setFixDescription((String) parameters.get("description"));
        fixRecord.setBeforeCodeUrl((String) parameters.get("beforeCodeUrl"));
        fixRecord.setAfterCodeUrl((String) parameters.get("afterCodeUrl"));
        fixRecord.setStatus(FixRecord.FixStatus.SUBMITTED);
        
        fixRecordRepository.insert(fixRecord);
        
        // 发送通知
        Issue issue = issueRepository.selectById(workflow.getIssueId());
        notificationService.sendFixRecordSubmittedNotification(fixRecord, issue);
    }
    
    /**
     * 更新整改记录状态
     */
    private void updateFixRecordStatus(FixTrackingWorkflow workflow, FixRecord.FixStatus status, 
                                     Map<String, Object> parameters) {
        FixRecord latestRecord = fixRecordRepository.findLatestByIssueId(workflow.getIssueId());
        if (latestRecord != null) {
            latestRecord.setStatus(status);
            latestRecord.setVerificationRemarks((String) parameters.get("remarks"));
            latestRecord.setVerifierId((Long) parameters.get("verifierId"));
            latestRecord.setVerifiedAt(LocalDateTime.now());
            
            fixRecordRepository.updateById(latestRecord);
            
            // 发送通知
            Issue issue = issueRepository.selectById(workflow.getIssueId());
            notificationService.sendFixRecordVerifiedNotification(latestRecord, issue);
        }
    }
    
    /**
     * 计算截止时间
     */
    private LocalDateTime calculateDeadline(Issue issue) {
        int days;
        switch (issue.getSeverity()) {
            case CRITICAL:
                days = 1;
                break;
            case MAJOR:
                days = 3;
                break;
            case MINOR:
                days = 7;
                break;
            case SUGGESTION:
                days = 14;
                break;
            default:
                days = 7;
        }
        
        return issue.getCreatedAt().plusDays(days);
    }
    
    /**
     * 获取超时的工作流
     */
    public List<FixTrackingWorkflow> getOverdueWorkflows() {
        log.info("获取超时的工作流");
        
        QueryWrapper<Issue> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("status", Issue.IssueStatus.OPEN, Issue.IssueStatus.IN_PROGRESS)
                .isNotNull("assigned_to");
        
        List<Issue> issues = issueRepository.selectList(queryWrapper);
        List<FixTrackingWorkflow> overdueWorkflows = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Issue issue : issues) {
            LocalDateTime deadline = calculateDeadline(issue);
            if (now.isAfter(deadline)) {
                FixTrackingWorkflow workflow = getCurrentWorkflow(issue.getId());
                if (workflow != null) {
                    workflow.setOverdue(true);
                    overdueWorkflows.add(workflow);
                }
            }
        }
        
        log.info("找到{}个超时的工作流", overdueWorkflows.size());
        return overdueWorkflows;
    }
    
    /**
     * 处理超时工作流
     */
    @Transactional
    public void handleOverdueWorkflows() {
        log.info("处理超时工作流");
        
        List<FixTrackingWorkflow> overdueWorkflows = getOverdueWorkflows();
        
        for (FixTrackingWorkflow workflow : overdueWorkflows) {
            try {
                // 升级问题严重级别
                escalateIssue(workflow.getIssueId());
                
                // 发送超时通知
                sendOverdueNotification(workflow);
                
                // 记录超时事件
                recordOverdueEvent(workflow);
                
            } catch (Exception e) {
                log.error("处理超时工作流失败: workflowId={}, error={}", 
                         workflow.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * 升级问题严重级别
     */
    private void escalateIssue(Long issueId) {
        Issue issue = issueRepository.selectById(issueId);
        if (issue != null) {
            // 提升严重级别
            if (issue.getSeverity() == com.company.codereview.common.enums.Severity.MINOR) {
                issue.setSeverity(com.company.codereview.common.enums.Severity.MAJOR);
            } else if (issue.getSeverity() == com.company.codereview.common.enums.Severity.MAJOR) {
                issue.setSeverity(com.company.codereview.common.enums.Severity.CRITICAL);
            }
            
            // 添加超时标记
            issue.setDescription(issue.getDescription() + "\n\n[系统提醒] 此问题已超时，已自动升级严重级别。");
            
            issueRepository.updateById(issue);
            
            // 发送升级通知
            notificationService.sendIssueEscalatedNotification(issue);
            
            log.info("问题升级成功: issueId={}, newSeverity={}", issueId, issue.getSeverity());
        }
    }
    
    /**
     * 发送超时通知
     */
    private void sendOverdueNotification(FixTrackingWorkflow workflow) {
        Issue issue = issueRepository.selectById(workflow.getIssueId());
        if (issue != null) {
            notificationService.sendOverdueReminderNotification(Arrays.asList(issue));
        }
    }
    
    /**
     * 记录超时事件
     */
    private void recordOverdueEvent(FixTrackingWorkflow workflow) {
        // 这里可以记录到审计日志或事件表中
        log.warn("工作流超时: issueId={}, assigneeId={}, deadline={}", 
                workflow.getIssueId(), workflow.getAssigneeId(), workflow.getDeadline());
    }
    
    /**
     * 获取工作流统计信息
     */
    public WorkflowStatistics getWorkflowStatistics(Long teamId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("获取工作流统计信息: teamId={}, startDate={}, endDate={}", teamId, startDate, endDate);
        
        List<Issue> issues = issueRepository.findByTeamId(teamId, startDate, endDate);
        
        WorkflowStatistics stats = new WorkflowStatistics();
        stats.setTotalWorkflows(issues.size());
        
        // 统计各阶段的工作流数量
        int completedCount = 0;
        int inProgressCount = 0;
        int overdueCount = 0;
        
        LocalDateTime now = LocalDateTime.now();
        
        for (Issue issue : issues) {
            if (issue.getStatus() == Issue.IssueStatus.RESOLVED || 
                issue.getStatus() == Issue.IssueStatus.CLOSED) {
                completedCount++;
            } else if (issue.getStatus() == Issue.IssueStatus.IN_PROGRESS) {
                inProgressCount++;
                
                // 检查是否超时
                LocalDateTime deadline = calculateDeadline(issue);
                if (now.isAfter(deadline)) {
                    overdueCount++;
                }
            }
        }
        
        stats.setCompletedWorkflows(completedCount);
        stats.setInProgressWorkflows(inProgressCount);
        stats.setOverdueWorkflows(overdueCount);
        
        // 计算完成率
        stats.setCompletionRate(issues.size() > 0 ? (double) completedCount / issues.size() * 100 : 0.0);
        
        // 计算平均处理时间
        List<Long> processingTimes = issues.stream()
                .filter(issue -> issue.getStatus() == Issue.IssueStatus.RESOLVED || 
                               issue.getStatus() == Issue.IssueStatus.CLOSED)
                .filter(issue -> issue.getUpdatedAt() != null)
                .map(issue -> java.time.Duration.between(issue.getCreatedAt(), issue.getUpdatedAt()).toHours())
                .collect(Collectors.toList());
        
        if (!processingTimes.isEmpty()) {
            stats.setAverageProcessingTime(processingTimes.stream().mapToLong(Long::longValue).average().orElse(0.0));
        }
        
        return stats;
    }
    
    // 枚举定义
    public enum WorkflowStage {
        CREATED("已创建"),
        ASSIGNED("已分配"),
        IN_PROGRESS("处理中"),
        UNDER_REVIEW("审核中"),
        REVISION_REQUIRED("需要修订"),
        COMPLETED("已完成"),
        CANCELLED("已取消");
        
        private final String description;
        
        WorkflowStage(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public enum WorkflowAction {
        ASSIGN("分配"),
        START_FIX("开始整改"),
        SUBMIT_FIX("提交整改"),
        APPROVE("批准"),
        REJECT("拒绝"),
        REQUEST_REVISION("要求修订"),
        RESUBMIT("重新提交"),
        CANCEL("取消");
        
        private final String description;
        
        WorkflowAction(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 内部类定义
    public static class FixTrackingWorkflow {
        private String id;
        private Long issueId;
        private Long assigneeId;
        private WorkflowStage currentStage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime lastActionTime;
        private LocalDateTime deadline;
        private boolean completed;
        private boolean overdue;
        
        public FixTrackingWorkflow() {
            this.id = UUID.randomUUID().toString();
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public Long getIssueId() { return issueId; }
        public void setIssueId(Long issueId) { this.issueId = issueId; }
        
        public Long getAssigneeId() { return assigneeId; }
        public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
        
        public WorkflowStage getCurrentStage() { return currentStage; }
        public void setCurrentStage(WorkflowStage currentStage) { this.currentStage = currentStage; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public LocalDateTime getLastActionTime() { return lastActionTime; }
        public void setLastActionTime(LocalDateTime lastActionTime) { this.lastActionTime = lastActionTime; }
        
        public LocalDateTime getDeadline() { return deadline; }
        public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
        
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        
        public boolean isOverdue() { return overdue; }
        public void setOverdue(boolean overdue) { this.overdue = overdue; }
    }
    
    public static class WorkflowStatistics {
        private int totalWorkflows;
        private int completedWorkflows;
        private int inProgressWorkflows;
        private int overdueWorkflows;
        private double completionRate;
        private double averageProcessingTime;
        
        // Getters and Setters
        public int getTotalWorkflows() { return totalWorkflows; }
        public void setTotalWorkflows(int totalWorkflows) { this.totalWorkflows = totalWorkflows; }
        
        public int getCompletedWorkflows() { return completedWorkflows; }
        public void setCompletedWorkflows(int completedWorkflows) { this.completedWorkflows = completedWorkflows; }
        
        public int getInProgressWorkflows() { return inProgressWorkflows; }
        public void setInProgressWorkflows(int inProgressWorkflows) { this.inProgressWorkflows = inProgressWorkflows; }
        
        public int getOverdueWorkflows() { return overdueWorkflows; }
        public void setOverdueWorkflows(int overdueWorkflows) { this.overdueWorkflows = overdueWorkflows; }
        
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
        
        public double getAverageProcessingTime() { return averageProcessingTime; }
        public void setAverageProcessingTime(double averageProcessingTime) { this.averageProcessingTime = averageProcessingTime; }
    }
}