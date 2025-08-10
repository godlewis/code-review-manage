package com.company.codereview.user.service;

import com.company.codereview.user.entity.Issue;
import com.company.codereview.user.entity.FixRecord;
import com.company.codereview.user.entity.User;
import com.company.codereview.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 问题通知服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueNotificationService {
    
    private final UserRepository userRepository;
    // 这里可以注入邮件服务、短信服务等
    
    /**
     * 发送问题创建通知
     */
    public void sendIssueCreatedNotification(Issue issue) {
        log.info("发送问题创建通知: issueId={}, title={}", issue.getId(), issue.getTitle());
        
        try {
            // 获取相关用户信息
            User creator = userRepository.selectById(issue.getCreatedBy());
            User assignee = issue.getAssignedTo() != null ? userRepository.selectById(issue.getAssignedTo()) : null;
            
            // 构建通知内容
            NotificationContent content = buildIssueCreatedContent(issue, creator, assignee);
            
            // 发送通知给分配的用户
            if (assignee != null) {
                sendNotification(assignee, content);
            }
            
            // 发送通知给团队负责人
            sendNotificationToTeamLeaders(issue, content);
            
        } catch (Exception e) {
            log.error("发送问题创建通知失败: issueId={}, error={}", issue.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 发送问题状态变更通知
     */
    public void sendIssueStatusChangedNotification(Issue issue, Issue.IssueStatus oldStatus, Issue.IssueStatus newStatus) {
        log.info("发送问题状态变更通知: issueId={}, oldStatus={}, newStatus={}", 
                issue.getId(), oldStatus, newStatus);
        
        try {
            User updater = userRepository.selectById(issue.getUpdatedBy());
            User assignee = issue.getAssignedTo() != null ? userRepository.selectById(issue.getAssignedTo()) : null;
            User creator = userRepository.selectById(issue.getCreatedBy());
            
            NotificationContent content = buildIssueStatusChangedContent(issue, oldStatus, newStatus, updater);
            
            // 通知相关用户
            if (assignee != null && !assignee.getId().equals(issue.getUpdatedBy())) {
                sendNotification(assignee, content);
            }
            
            if (creator != null && !creator.getId().equals(issue.getUpdatedBy()) && 
                (assignee == null || !creator.getId().equals(assignee.getId()))) {
                sendNotification(creator, content);
            }
            
        } catch (Exception e) {
            log.error("发送问题状态变更通知失败: issueId={}, error={}", issue.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 发送问题分配通知
     */
    public void sendIssueAssignedNotification(Issue issue, Long oldAssigneeId, Long newAssigneeId) {
        log.info("发送问题分配通知: issueId={}, oldAssigneeId={}, newAssigneeId={}", 
                issue.getId(), oldAssigneeId, newAssigneeId);
        
        try {
            User newAssignee = newAssigneeId != null ? userRepository.selectById(newAssigneeId) : null;
            User oldAssignee = oldAssigneeId != null ? userRepository.selectById(oldAssigneeId) : null;
            User assigner = userRepository.selectById(issue.getUpdatedBy());
            
            // 通知新分配的用户
            if (newAssignee != null) {
                NotificationContent content = buildIssueAssignedContent(issue, assigner, true);
                sendNotification(newAssignee, content);
            }
            
            // 通知原分配的用户
            if (oldAssignee != null && !oldAssignee.getId().equals(newAssigneeId)) {
                NotificationContent content = buildIssueAssignedContent(issue, assigner, false);
                sendNotification(oldAssignee, content);
            }
            
        } catch (Exception e) {
            log.error("发送问题分配通知失败: issueId={}, error={}", issue.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 发送问题升级通知
     */
    public void sendIssueEscalatedNotification(Issue issue) {
        log.info("发送问题升级通知: issueId={}, severity={}", issue.getId(), issue.getSeverity());
        
        try {
            User assignee = issue.getAssignedTo() != null ? userRepository.selectById(issue.getAssignedTo()) : null;
            User creator = userRepository.selectById(issue.getCreatedBy());
            
            NotificationContent content = buildIssueEscalatedContent(issue);
            
            // 通知相关用户
            if (assignee != null) {
                sendNotification(assignee, content);
            }
            
            if (creator != null && (assignee == null || !creator.getId().equals(assignee.getId()))) {
                sendNotification(creator, content);
            }
            
            // 通知团队负责人
            sendNotificationToTeamLeaders(issue, content);
            
        } catch (Exception e) {
            log.error("发送问题升级通知失败: issueId={}, error={}", issue.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 发送整改记录提交通知
     */
    public void sendFixRecordSubmittedNotification(FixRecord fixRecord, Issue issue) {
        log.info("发送整改记录提交通知: fixRecordId={}, issueId={}", fixRecord.getId(), issue.getId());
        
        try {
            User fixer = userRepository.selectById(fixRecord.getFixerId());
            User creator = userRepository.selectById(issue.getCreatedBy());
            
            NotificationContent content = buildFixRecordSubmittedContent(fixRecord, issue, fixer);
            
            // 通知问题创建者
            if (creator != null && !creator.getId().equals(fixRecord.getFixerId())) {
                sendNotification(creator, content);
            }
            
            // 通知验证人
            if (fixRecord.getVerifierId() != null) {
                User verifier = userRepository.selectById(fixRecord.getVerifierId());
                if (verifier != null) {
                    sendNotification(verifier, content);
                }
            }
            
        } catch (Exception e) {
            log.error("发送整改记录提交通知失败: fixRecordId={}, error={}", fixRecord.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 发送整改记录验证通知
     */
    public void sendFixRecordVerifiedNotification(FixRecord fixRecord, Issue issue) {
        log.info("发送整改记录验证通知: fixRecordId={}, result={}", 
                fixRecord.getId(), fixRecord.getVerificationResult());
        
        try {
            User verifier = userRepository.selectById(fixRecord.getVerifierId());
            User fixer = userRepository.selectById(fixRecord.getFixerId());
            User creator = userRepository.selectById(issue.getCreatedBy());
            
            NotificationContent content = buildFixRecordVerifiedContent(fixRecord, issue, verifier);
            
            // 通知整改人
            if (fixer != null && !fixer.getId().equals(fixRecord.getVerifierId())) {
                sendNotification(fixer, content);
            }
            
            // 通知问题创建者
            if (creator != null && !creator.getId().equals(fixRecord.getVerifierId()) && 
                !creator.getId().equals(fixRecord.getFixerId())) {
                sendNotification(creator, content);
            }
            
        } catch (Exception e) {
            log.error("发送整改记录验证通知失败: fixRecordId={}, error={}", fixRecord.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * 发送超时提醒通知
     */
    public void sendOverdueReminderNotification(List<Issue> overdueIssues) {
        log.info("发送超时提醒通知: issueCount={}", overdueIssues.size());
        
        for (Issue issue : overdueIssues) {
            try {
                User assignee = issue.getAssignedTo() != null ? userRepository.selectById(issue.getAssignedTo()) : null;
                
                if (assignee != null) {
                    NotificationContent content = buildOverdueReminderContent(issue);
                    sendNotification(assignee, content);
                }
                
            } catch (Exception e) {
                log.error("发送超时提醒通知失败: issueId={}, error={}", issue.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * 发送通知给团队负责人
     */
    private void sendNotificationToTeamLeaders(Issue issue, NotificationContent content) {
        // 这里需要根据实际的团队管理逻辑来获取团队负责人
        // 暂时跳过实现
        log.debug("发送通知给团队负责人: issueId={}", issue.getId());
    }
    
    /**
     * 发送通知
     */
    private void sendNotification(User user, NotificationContent content) {
        log.info("发送通知: userId={}, title={}", user.getId(), content.getTitle());
        
        // 这里可以根据用户偏好选择通知方式：邮件、短信、站内信等
        // 暂时只记录日志
        log.debug("通知内容: {}", content.getContent());
        
        // TODO: 实现具体的通知发送逻辑
        // emailService.sendEmail(user.getEmail(), content.getTitle(), content.getContent());
        // smsService.sendSms(user.getPhone(), content.getContent());
        // inAppNotificationService.sendNotification(user.getId(), content);
    }
    
    // 构建通知内容的方法
    private NotificationContent buildIssueCreatedContent(Issue issue, User creator, User assignee) {
        String title = "新问题已创建: " + issue.getTitle();
        String content = String.format(
                "问题详情:\\n" +
                "标题: %s\\n" +
                "类型: %s\\n" +
                "严重级别: %s\\n" +
                "描述: %s\\n" +
                "创建人: %s\\n" +
                "分配给: %s\\n" +
                "创建时间: %s",
                issue.getTitle(),
                issue.getIssueType(),
                issue.getSeverity(),
                issue.getDescription(),
                creator != null ? creator.getUsername() : "未知",
                assignee != null ? assignee.getUsername() : "未分配",
                issue.getCreatedAt()
        );
        
        return new NotificationContent(title, content, NotificationType.ISSUE_CREATED);
    }
    
    private NotificationContent buildIssueStatusChangedContent(Issue issue, Issue.IssueStatus oldStatus, 
                                                              Issue.IssueStatus newStatus, User updater) {
        String title = "问题状态已更新: " + issue.getTitle();
        String content = String.format(
                "问题状态变更:\\n" +
                "标题: %s\\n" +
                "原状态: %s\\n" +
                "新状态: %s\\n" +
                "更新人: %s\\n" +
                "更新时间: %s",
                issue.getTitle(),
                oldStatus,
                newStatus,
                updater != null ? updater.getUsername() : "未知",
                LocalDateTime.now()
        );
        
        return new NotificationContent(title, content, NotificationType.ISSUE_STATUS_CHANGED);
    }
    
    private NotificationContent buildIssueAssignedContent(Issue issue, User assigner, boolean isNewAssignment) {
        String title = (isNewAssignment ? "问题已分配给您: " : "问题分配已取消: ") + issue.getTitle();
        String content = String.format(
                "问题分配%s:\\n" +
                "标题: %s\\n" +
                "类型: %s\\n" +
                "严重级别: %s\\n" +
                "操作人: %s\\n" +
                "时间: %s",
                isNewAssignment ? "" : "取消",
                issue.getTitle(),
                issue.getIssueType(),
                issue.getSeverity(),
                assigner != null ? assigner.getUsername() : "未知",
                LocalDateTime.now()
        );
        
        return new NotificationContent(title, content, NotificationType.ISSUE_ASSIGNED);
    }
    
    private NotificationContent buildIssueEscalatedContent(Issue issue) {
        String title = "问题已升级: " + issue.getTitle();
        String content = String.format(
                "问题升级通知:\\n" +
                "标题: %s\\n" +
                "当前严重级别: %s\\n" +
                "原因: 问题超时未处理\\n" +
                "请尽快处理此问题",
                issue.getTitle(),
                issue.getSeverity()
        );
        
        return new NotificationContent(title, content, NotificationType.ISSUE_ESCALATED);
    }
    
    private NotificationContent buildFixRecordSubmittedContent(FixRecord fixRecord, Issue issue, User fixer) {
        String title = "整改记录已提交: " + issue.getTitle();
        String content = String.format(
                "整改记录提交:\\n" +
                "问题标题: %s\\n" +
                "整改人: %s\\n" +
                "整改描述: %s\\n" +
                "提交时间: %s\\n" +
                "请及时验证整改结果",
                issue.getTitle(),
                fixer != null ? fixer.getUsername() : "未知",
                fixRecord.getFixDescription(),
                fixRecord.getCreatedAt()
        );
        
        return new NotificationContent(title, content, NotificationType.FIX_RECORD_SUBMITTED);
    }
    
    private NotificationContent buildFixRecordVerifiedContent(FixRecord fixRecord, Issue issue, User verifier) {
        String title = "整改记录验证完成: " + issue.getTitle();
        String content = String.format(
                "整改记录验证结果:\\n" +
                "问题标题: %s\\n" +
                "验证人: %s\\n" +
                "验证结果: %s\\n" +
                "验证备注: %s\\n" +
                "验证时间: %s",
                issue.getTitle(),
                verifier != null ? verifier.getUsername() : "未知",
                fixRecord.getVerificationResult(),
                fixRecord.getVerificationRemarks(),
                fixRecord.getVerifiedAt()
        );
        
        return new NotificationContent(title, content, NotificationType.FIX_RECORD_VERIFIED);
    }
    
    private NotificationContent buildOverdueReminderContent(Issue issue) {
        String title = "问题处理超时提醒: " + issue.getTitle();
        String content = String.format(
                "问题处理超时提醒:\\n" +
                "标题: %s\\n" +
                "严重级别: %s\\n" +
                "创建时间: %s\\n" +
                "该问题已超时，请尽快处理",
                issue.getTitle(),
                issue.getSeverity(),
                issue.getCreatedAt()
        );
        
        return new NotificationContent(title, content, NotificationType.OVERDUE_REMINDER);
    }
    
    // 内部类定义
    public static class NotificationContent {
        private String title;
        private String content;
        private NotificationType type;
        
        public NotificationContent(String title, String content, NotificationType type) {
            this.title = title;
            this.content = content;
            this.type = type;
        }
        
        // Getters and Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public NotificationType getType() { return type; }
        public void setType(NotificationType type) { this.type = type; }
    }
    
    public enum NotificationType {
        ISSUE_CREATED,
        ISSUE_STATUS_CHANGED,
        ISSUE_ASSIGNED,
        ISSUE_ESCALATED,
        FIX_RECORD_SUBMITTED,
        FIX_RECORD_VERIFIED,
        OVERDUE_REMINDER
    }
}